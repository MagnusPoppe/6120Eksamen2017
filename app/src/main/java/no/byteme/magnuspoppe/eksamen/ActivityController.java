package no.byteme.magnuspoppe.eksamen;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;

import no.byteme.magnuspoppe.eksamen.datamodel.Bruker;
import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;
import no.byteme.magnuspoppe.eksamen.datamodel.DestinasjonDB;

/**
 * Dette er kontrolleren for hele applikasjonen.
 */
public class ActivityController extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        mellomLagerKontrakt
{

    // Konstanter
    public static final String HOVED_LATITIUDE = "kldaføjsefølakjdf";
    public static final String HOVED_LONGITUDE = "asløkdsalskdjfkal";
    private static final String DESTINASJONSLISTE = "jlkd";

    private static final LatLng HOYSKOLEN = new LatLng(59.408852, 9.059512);
    public static final String INNSTILLINGER = "no.byteme.magnuspoppe.eksamen.preferences";
    public static final String FOTO_LAGER="images";
    // "STATE":
    private boolean detaljinfoVises;

    // Klient som kommuniserer med Google Play API
    private GoogleApiClient mGoogleApiClient = null;

    // Kartverdier:
    private LatLng enhetensPosisjon;
    private Location lokasjon = null;
    private FragmentMap kart;
    private FragmentCloseLocationList destinasjonsliste;

    // Grafiske views:
    private LinearLayout bunnPanel;
    private LinearLayout kartPanel;
    private FloatingActionButton leggTilKnapp;

    // Datamodell:
    private Bruker bruker;
    private ArrayList<Destinasjon> destinasjoner;

    private DestinasjonDB db;

    //---------------------------------------------------------------
    //      Metoder som hører med aktivitetens livssyklus
    //---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Henter bruker fra "SharedPreferences"
        brukerOppsett();

        // Setter opp nødvendig GUI:
        Toolbar appLinje = (Toolbar) findViewById(R.id.appLinje);
        setActionBar(appLinje);
        leggTilKnapp = (FloatingActionButton) findViewById(R.id.leggTil);
        animasjonsOppsett();

        db = new DestinasjonDB(getApplicationContext());

        if(savedInstanceState == null || !savedInstanceState.containsKey(DESTINASJONSLISTE))
        {
            // Henter ut destinasjonsdata asynkront:
            destinasjoner = new ArrayList<>();
            oppdaterDatasett();
        }
        else // Inneholder lagrede objekter:
        {
            destinasjoner = savedInstanceState.getParcelableArrayList(DESTINASJONSLISTE);
            oppdaterDatasett();
        }

        // Lager Google API Klient objekt:
        if (mGoogleApiClient == null) {
            GoogleApiClient.Builder apiBuilder = new GoogleApiClient.Builder(this);
            apiBuilder.addConnectionCallbacks(this);        /* ConnectionCallbacks-objekt */
            apiBuilder.addOnConnectionFailedListener(this); /* OnConnectionFailedListener-objekt */
            apiBuilder.addApi(LocationServices.API);        /* Velg Play Service API */
            mGoogleApiClient = apiBuilder.build();
        }

        // Lager kart og listepanel:
        enhetensPosisjon = null;
        visKart();
        visDestinasjonsListe();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Connect the ApiClient to Google Services
        mGoogleApiClient.connect();

        // Kobler til DB:
        db.open();
    }

    @Override
    protected void onStop()
    {
        // Disconnect the ApiClient
        mGoogleApiClient.disconnect();

        // Kobler fra DB:
        db.close();

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putParcelableArrayList(DESTINASJONSLISTE, destinasjoner);
        super.onSaveInstanceState(outState);
    }

    //---------------------------------------------------------------
    //      Metoder for styring av fragmenter
    //---------------------------------------------------------------

    /**
     * Lager og viser innstillingsvindu.
     */
    protected void visInnstillinger()
    {
        // Skjuler "legg til" knapp (blir satt tilbake når listen vises igjen):
        getLeggTilKnapp().setVisibility(View.GONE);

        visInnstillingPanel(true);
        Preferences innstillingFragment = new Preferences();
        FragmentTransaction transaksjon = getFragmentManager().beginTransaction();
        transaksjon.replace(R.id.innstillingFragmentHolder, innstillingFragment);
        transaksjon.addToBackStack(null);

        // Utfører transaksjonen.
        transaksjon.commit();
    }

    /**
     * Viser innstillingspanel-holderen for å få hvit bakgrunn på
     * innstillingsvindu. Dette er egendefinert HACK for å unngå transparent
     * bakgrunn.
     * @param vis panel eller ikke vis panel
     */
    public void visInnstillingPanel(boolean vis)
    {
        LinearLayout panel = (LinearLayout) findViewById(R.id.innstillingFragmentHolder);
        if (vis) panel.setVisibility(View.VISIBLE);
        else panel.setVisibility(View.GONE);
    }

    /**
     * Initialiserer lokasjonsliste fragmentet. Siden dette er første
     * fragment legges det ikke til i lokasjonslisten.
     */
    private void visDestinasjonsListe()
    {
        // Plasserer ut listen:
        destinasjonsliste = new FragmentCloseLocationList();
        FragmentTransaction transaksjon = getFragmentManager().beginTransaction();
        transaksjon.replace(R.id.ListeFragmentHolder, destinasjonsliste);

        // Utfører transaksjonen.
        transaksjon.commit();
    }

    /**
     * Initialiserer detaljertInfo fragmentet. Dette legges på
     * bak-stakken så man kan hoppe tilbake til listen med tilbake-
     * knapp.
     */
    public void visDetaljertInformasjonsPanel(int indeksDestinasjon)
    {
        // Vi vil alltid at når man klikker tilbakeknappen skal man
        // se listen. Vi fjerner derfor et lag om vi allerede viser detaljinfo vinduet
        // når et nytt hentes inn.
        if (detaljinfoVises)
        {
            getFragmentManager().popBackStack();

            // Skjuler "legg til" knapp (blir satt tilbake når listen vises igjen):
            getLeggTilKnapp().setVisibility(View.GONE);
        }



        // Skalerer om vindu for å flytte brukerens fokus:
        skalerPanelVekting(0.6f);

        // Plasserer ut listen:
        FragmentDetailedInfo detaljinfo = new FragmentDetailedInfo();
        FragmentTransaction transaksjon = getFragmentManager().beginTransaction();
        transaksjon.replace(R.id.ListeFragmentHolder, detaljinfo);
        transaksjon.addToBackStack(null);

        // Legger ved hvilken destinasjon som ble valgt.
        Bundle argumenter = new Bundle();
        argumenter.putInt("SELECTED_DESTINATION", indeksDestinasjon);
        detaljinfo.setArguments(argumenter);

        // Utfører transaksjonen.
        transaksjon.commit();
    }

    /**
     * Viser detaljert informasjonspanel med et gitt destinasjonsobjekt.
     * @param destinasjon som skal vises.
     */
    public void visDetaljertInformasjonsPanel(Destinasjon destinasjon)
    {
        for (int i = 0; i < destinasjoner.size(); i++)
        {
            Destinasjon andre = destinasjoner.get(i);
            if (andre.equals(destinasjon))
            {
                visDetaljertInformasjonsPanel(i);
                return;
            }
        }
    }

    /**
     * Viser "legg til panelet i vinduet. Kartet skal sentrers til posisjonen
     * brukeren har og kan ikke flyttes nå. TODO!
     */
    private void visLeggTilLokasjon()
    {
        if (lokasjon == null)
        {
            Snackbar.make(this.getCurrentFocus(), "Lokasjon ikke enda funnet.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Plasserer ut legg-til panlet:
        FragmentAddLocation leggTil = new FragmentAddLocation();
        FragmentTransaction transaksjon = getFragmentManager().beginTransaction();
        transaksjon.replace(R.id.ListeFragmentHolder, leggTil);
        transaksjon.addToBackStack(null);

        // Legger ved informasjon om nåværende posisjon:
        Bundle argumenter = new Bundle();
        argumenter.putDouble("MOH", lokasjon.getAltitude());
        argumenter.putDouble("LAT", lokasjon.getLatitude());
        argumenter.putDouble("LNG", lokasjon.getLongitude());
        leggTil.setArguments(argumenter);

        // Utfører transaksjonen.
        transaksjon.commit();
    }

    /**
     * Viser kartet uten parametere. Dette vil gjøre at
     * kartfragmentet (FragmentMap) vil gjøre sin standard
     * visning av enhetens koordinater.
     */
    private void visKart()
    {
        visKart(null);
    }

    /**
     * Viser kartet som fragment inn i "mapFragmentContainer"
     * @param koordinater kartet skal sentrere seg rundt.
     */
    private void visKart(LatLng koordinater)
    {
        // Henter nødvendige data
        kart = new FragmentMap();
        FragmentTransaction transaksjon = getFragmentManager().beginTransaction();
        transaksjon.replace(R.id.kartFragmentHolder, kart);

        // Legger ved koordinater som skal vises i kartet:
        if (koordinater != null)
        {
            Bundle argumenter = new Bundle();
            argumenter.putDouble(HOVED_LATITIUDE, koordinater.latitude);
            argumenter.putDouble(HOVED_LONGITUDE, koordinater.longitude);
            kart.setArguments(argumenter);
        }

        // Utfører transaksjonen.
        transaksjon.commit();
    }

    /**
     * Endrer vektingen av de to panelene vist. Disse skal være fokusert på der det er mest
     * viktig informasjon.
     * @param vekt som skal settes på bunn-panelet.
     */
    public void skalerPanelVekting(float vekt)
    {
        // Skalerer bunnpanelet til oppgitt vekt:
        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) bunnPanel.getLayoutParams();
        param.weight = vekt;
        bunnPanel.setLayoutParams(param);

        // Skalerer kartpanel til Kompliment av vekting:
        LinearLayout.LayoutParams kartParam = (LinearLayout.LayoutParams) kartPanel.getLayoutParams();
        kartParam.weight = (1-vekt);
        kartPanel.setLayoutParams(kartParam);
    }

    //---------------------------------------------------------------
    //      Database operasjoner:
    //---------------------------------------------------------------

    /**
     * Oppdaterer datasettet for appen.
     */
    private void oppdaterDatasett()
    {
        // Henter ut datasett:
        if (enhetPåNett())
        {
            AsynkronDestinasjon oppgave = new AsynkronDestinasjon(this);
            oppgave.get();
        }
    }

    /**
     * Gjør spørring mot lokal database for å se om det er oppføringer
     * i databasen som ikke finnes på nett. Hvis finnes, skal de lastes opp,
     * så slettes lokalt. Slettingen gjøres ved CallBack.
     * Callback metode: ActivityController.vedKomplettOpplastingAvDestinasjoner()
     */
    protected void lastOppMidlertidigLagret()
    {
        if (enhetPåNett())
        {
            Destinasjon[] uopplastet = db.getAlleUopplastedeDestinasjoner();

            // Hvis det var svar i resultatet:
            if (uopplastet != null)
            {
                // Starter asynkron oppgave for å laste opp destinasjoner:
                AsynkronDestinasjon asynk = new AsynkronDestinasjon(this);
                asynk.postUopplastet(uopplastet);
            }
        }
    }

    /**
     * Callback for når uoplastende lokalt lagrede elementer har
     * blitt lastet opp til den globale databasen.
     * @param ider på de opplastede elementene.
     */
    public void vedKomplettOpplastingAvDestinasjoner(int[] ider)
    {
        // Sletter de nødvendige oppføringene:
        db.deleteDestinasjon(ider);

        // Informer bruker:
        if( getCurrentFocus()!=null)
            Snackbar.make(getCurrentFocus(), "Turmål er publisert til tjener!",
                    Snackbar.LENGTH_SHORT).show();

        // Oppdaterer datasettet med nye oppføringer.
        oppdaterDatasett();
    }

    //---------------------------------------------------------------
    //      Metoder som Lokasjonshåndtering og destinasjoner
    //---------------------------------------------------------------

    /**
     * @return Enhetens lokasjon
     */
    public LatLng getEnhetensPosisjon()
    {
        if (enhetensPosisjon != null)
            return enhetensPosisjon;
        else
            return HOYSKOLEN;
    }

    /**
     * @param posisjon setter ny posisjon for enhet.
     */
    private void setEnhetensPosisjon(LatLng posisjon)
    {
        this.enhetensPosisjon = posisjon;
        sorterDestinasjoner();


        if (kart.isBrukerEnhetPosisjon())
            kart.oppdaterBrukerPosisjon(posisjon);
    }

    /**
     * Legger til en destinasjon på sin korrekte sorterte plass i
     * ArrayListen av destinasjoner.
     * @param destinasjon
     */
    public void leggTilDestinasjon(Destinasjon destinasjon)
    {
        for (int i = 0; i < destinasjoner.size(); i++)
        {
            Destinasjon andre = destinasjoner.get(i);

            if (destinasjon.compareTo(andre) < 0)
            {
                destinasjoner.add(i, destinasjon);
                return;
            }
        }
    }

    /**
     * Setter destinasjonsobjektet og sorterer det etter enhetens posisjon.
     * @param destinasjoner
     */
    public void setDestinasjoner(ArrayList<Destinasjon> destinasjoner)
    {
        // Rydder vekk alle markører på kartet:
        // if (kart != null)
        //     kart.fjernAlleMarkorer();

        // Setter og sorterer ny tabell:
        this.destinasjoner = destinasjoner;
        sorterDestinasjoner();
    }

    /**
     * Markerer kartet med markører for alle destinasjoner.
     */
    public void settUtAlleMarkorer()
    {
        if (kart != null)
        {
            kart.fjernAlleMarkorer();
            for (Destinasjon destinasjon : destinasjoner)
                kart.markerKartet(destinasjon, destinasjon.getKoordinat());
        }
    }

    /**
     * Sorterer alle destinasjoner etter avstand fra bruker. Nærmeste først.
     * (bruker enhetens Posisjon).
     *
     * Denne metoden kalles på enten når enhetens lokasjon har endret seg,
     * eller når destinasjonslisten har endret seg.
     */
    public void sorterDestinasjoner()
    {
        // Lager ny tabell for å sortere i.
        Destinasjon[] sorterbar = new Destinasjon[destinasjoner.size()];
        int i = 0;

        // Løper igjennom alle objekter og oppdaterer avstand fra enheten:
        for( Destinasjon destinasjon : destinasjoner)
        {
            // Beregner avstand mellom destinasjon og enhet
            float[] resultater = new float[10];
            Location.distanceBetween(
                    getEnhetensPosisjon().latitude, getEnhetensPosisjon().longitude,
                    destinasjon.getKoordinat().latitude, destinasjon.getKoordinat().longitude,
                    resultater
            );

            // Setter avstand og markerer kartet med destinasjonen.
            destinasjon.setAvstandFraEnhet(resultater[0]);
            sorterbar[i++] = destinasjon; // Legger til elemeneter inn i en sorterbar tabell
        }

        // Sorterer tabellen:
        Arrays.sort(sorterbar);

        // Oppdaterer arraylist for visning:
        destinasjoner.clear();
        for( Destinasjon destinasjon : sorterbar )
            destinasjoner.add(destinasjon);

        // Oppdaterer view hvis på hovedthread.
        if (destinasjonsliste != null && Looper.myLooper() == Looper.getMainLooper())
        {
            destinasjonsliste.oppdaterListen();
            settUtAlleMarkorer();
        }
    }

    /**
     * "Eventhandeler" for min lokasjon knappen. Flytter kartet til min
     * lokasjon.
     */
    public void gåTilLokasjon()
    {
        kart.oppdaterBrukerPosisjon(getEnhetensPosisjon());
    }

    /**
     * "Eventhandeler" for legg til knapp.
     * Denne kontrollerer om bruker er klar for å legge til lokasjon.
     * Dette krever brukerdata og lokasjonsdata.
     * @param view
     */
    public void leggTilLokasjon(View view)
    {
        FloatingActionButton fab = (FloatingActionButton) view;

        if (bruker == null)
        {
            Snackbar.make(view, "Ingen registrert bruker.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("REGISTRER", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {visInnstillinger();
                        }
                    })
                    .show();
            return;
        }
        if (! bruker.harKorrektEpost())
        {
            Snackbar.make(view, "Feil format på E-Post.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("REGISTRER", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {visInnstillinger();
                        }
                    })
                    .show();
            return;
        }
        if (lokasjon == null)
        {
            Snackbar.make(view, "Kan ikke legge til uten enhetens lokasjon.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        fab.setVisibility(View.GONE);
        skalerPanelVekting(0.8f);
        visLeggTilLokasjon();
    }

    //---------------------------------------------------------------
    //      Metoder som hører med Oppsett
    //---------------------------------------------------------------

    /**
     * Lager en bruker ut ifra "sharedpreferences". Hvis ingen bruker
     * er registrert vil brukeren bli satt til NULL.
     */
    public void brukerOppsett()
    {
        SharedPreferences preferences = getSharedPreferences(INNSTILLINGER, MODE_PRIVATE);

        if (preferences.contains("email"))
        {
            bruker = new Bruker(
                    preferences.getString("email", ""),
                    preferences.getString("firstName", ""),
                    preferences.getString("lastName", "")
            );
        }
        else {
            bruker = null;
        }
    }

    /**
     * Setter opp menyen
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menyView = getMenuInflater();
        menyView.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Setter opp animasjon for bruk ved skiftende vindustørrelser.
     */
    public void animasjonsOppsett()
    {
        // Setter animasjon på panelene:
        bunnPanel = (LinearLayout) findViewById(R.id.ListeFragmentHolder);
        kartPanel = (LinearLayout) findViewById(R.id.kartFragmentHolder);
        LayoutTransition overgang = new LayoutTransition();
        overgang.enableTransitionType(LayoutTransition.CHANGING);
        overgang.disableTransitionType(LayoutTransition.APPEARING);
        overgang.disableTransitionType(LayoutTransition.DISAPPEARING);
        overgang.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        overgang.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        overgang.setDuration(400);
        bunnPanel.setLayoutTransition(overgang);
        kartPanel.setLayoutTransition(overgang);
    }

    /**
     * "Event handeler" for når menyen blir brukt.
     * @param valgt oppføring i menyen
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem valgt) {
        // Håndtere knappeklikk fra bruker:
        switch (valgt.getItemId()) {
            case R.id.innstillinger:
                visInnstillinger();
                return true;
            case R.id.oppdaterLokasjon:
                gåTilLokasjon();
                return true;
            case R.id.oppdaterDestinasjoner:
                oppdaterDatasett();
                lastOppMidlertidigLagret();
                return true;
            default:
                return super.onOptionsItemSelected(valgt);
        }
    }


    //---------------------------------------------------------------
    //      SETTERS OG GETTERS:
    //---------------------------------------------------------------

    /**
     * @param detaljinfoVises
     */
    public void setDetaljinfoVises(boolean detaljinfoVises)
    {
        this.detaljinfoVises = detaljinfoVises;
    }

    /**
     * @return Alle destinasjonsobjekter i form av ArrayList<Destinasjon>
     */
    public ArrayList<Destinasjon> getDestinasjoner()
    {
        return destinasjoner;
    }

    /**
     * @return ferdig konfigurert kart.
     */
    public FragmentMap getKart()
    {
        return kart;
    }

    /**
     * @return Legg til knappens objekt. (FAB)
     */
    public FloatingActionButton getLeggTilKnapp()
    {
        return leggTilKnapp;
    }

    /**
     * @return Databaseobjekt for bruk av SQLite database.
     */
    public DestinasjonDB getDB()
    {
        return db;
    }

    //---------------------------------------------------------------
    //      Metoder for status av telefonen (lokasjon og nett)
    //---------------------------------------------------------------

    /**
     * Sjekker om enheten er koblet til internett. Dette gjøres for
     * å unngå feil ved asynkrone oppgaver.
     * @return true hvis internett, false hvis ikke.
     */
    public boolean enhetPåNett()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(
                Activity.CONNECTIVITY_SERVICE
        );
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //---------------------------------------------------------------
    //      FØLGENDE ER KOPI FRA LEKSJON 12B.
    //      Modifisert for å passe applikasjonen.
    //---------------------------------------------------------------

    public final static int REQUEST_LOCATION = 1;

    /**
     * Utføres når oppkobling skjer.
     * Direkte kopi fra leksjon 12b.
     */
    @Override
    public void onConnected(Bundle connectionHint)
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // OK: Appen har tillatelsen ACCESS_FINE_LOCATION. Finn siste posisjon
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null){
                this.setEnhetensPosisjon(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                lokasjon = lastLocation;
                getLeggTilKnapp().setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Utføres når koblingen feiler
     * Direkte kopi fra leksjon 12b.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle the failure silently
        if (getCurrentFocus() != null)
            Snackbar.make(getCurrentFocus(),
                    "Klarte ikke å koble til Google Play Services", Snackbar.LENGTH_LONG).show();
    }

    /**
     * Utføres når koblingen blir utsatt
     * Direkte kopi fra leksjon 12b.
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Callbackmetode som kalles etter at bruker har svart på spørsmål om rettigheter
     * Direkte kopi fra leksjon 12b.
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    lokasjon = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (lokasjon != null)
                        this.setEnhetensPosisjon(new LatLng(lokasjon.getLatitude(), lokasjon.getLongitude()));
                }
                catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else {
                // Permission was denied or request was cancelled
                Snackbar.make(getCurrentFocus(),
                        "Kan ikke vise posisjon uten tillatelse", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
