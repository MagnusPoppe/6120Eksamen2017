package no.byteme.magnuspoppe.eksamen;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

public class ActivityMain extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    private static final LatLng HOYSKOLEN = new LatLng(59.408852, 9.059512);

    public static final String HOVED_LATITIUDE = "kldaføjsefølakjdf";
    public static final String HOVED_LONGITUDE = "asløkdsalskdjfkal";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 978123;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 987122;

    private Location lokasjon = null;
    // Klient som kommuniserer med Google Play API
    private GoogleApiClient mGoogleApiClient = null;

    private LatLng enhetensPosisjon;
    private FragmentMap kart;
    private FragmentCloseLocationList destinasjonsliste;

    private LinearLayout bunnPanel;
    private LinearLayout kartPanel;

    private FloatingActionButton leggTilKnapp;
    private ArrayList<Destinasjon> destinasjoner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leggTilKnapp = (FloatingActionButton) findViewById(R.id.leggTil);

        // Henter ut destinasjonsdata asynkront:
        destinasjoner = new ArrayList<>();
        AsynkronDestinasjon oppgave = new AsynkronDestinasjon(this);
        oppgave.get();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            GoogleApiClient.Builder apiBuilder = new GoogleApiClient.Builder(this);
            apiBuilder.addConnectionCallbacks(this);        /* ConnectionCallbacks-objekt */
            apiBuilder.addOnConnectionFailedListener(this); /* OnConnectionFailedListener-objekt */
            apiBuilder.addApi(LocationServices.API);        /* Velg Play Service API */
            mGoogleApiClient = apiBuilder.build();
        }

        // Setter animasjon på panelene:
        bunnPanel = (LinearLayout) findViewById(R.id.locationsListFragmentContainer);
        kartPanel = (LinearLayout) findViewById(R.id.mapFragmentContainer);
        LayoutTransition overgang = new LayoutTransition();
        overgang.enableTransitionType(LayoutTransition.CHANGING);
        overgang.disableTransitionType(LayoutTransition.APPEARING);
        overgang.disableTransitionType(LayoutTransition.DISAPPEARING);
        overgang.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        overgang.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        overgang.setDuration(400);
        bunnPanel.setLayoutTransition(overgang);
        kartPanel.setLayoutTransition(overgang);

        // Lager kart og listepanel:
        enhetensPosisjon = null;
        visKart();
        visDestinasjonsListe();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the ApiClient to Google Services
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnect the ApiClient
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Flytter kameraet til en posisjon.
     * @param position å flytte til.
     */
    public void flyttKameraTil(LatLng position)
    {
        kart.flyttKameraTil(position);
    }

    /**
     * Flytter kameraet til en posisjon og setter en markør i posisjonen.
     * @param title tekst på markøren
     * @param position posisjonen alt skal skje på.
     */
    public void flyttTilOgMarker(String title, LatLng position)
    {
        kart.flyttKameraTil(position);
        kart.markerKartet(title, position);
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

    /**
     * Initialiserer lokasjonsliste fragmentet. Siden dette er første
     * fragment legges det ikke til i lokasjonslisten.
     */
    private void visDestinasjonsListe()
    {
        // Plasserer ut listen:
        destinasjonsliste = new FragmentCloseLocationList();
        FragmentTransaction transaksjon = getFragmentManager().beginTransaction();
        transaksjon.replace(R.id.locationsListFragmentContainer, destinasjonsliste);

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
        // Plasserer ut listen:
        FragmentDetailedInfo detaljinfo = new FragmentDetailedInfo();
        FragmentTransaction transaksjon = getFragmentManager().beginTransaction();
        transaksjon.replace(R.id.locationsListFragmentContainer, detaljinfo);
        transaksjon.addToBackStack(null);

        // Legger ved hvilken destinasjon som ble valgt.
        Bundle argumenter = new Bundle();
        argumenter.putInt("SELECTED_DESTINATION", indeksDestinasjon);
        detaljinfo.setArguments(argumenter);

        // Utfører transaksjonen.
        transaksjon.commit();
    }


    private void visLeggTilLokasjon()
    {
        // Plasserer ut listen:
        FragmentAddLocation leggTil = new FragmentAddLocation();
        FragmentTransaction transaksjon = getFragmentManager().beginTransaction();
        transaksjon.replace(R.id.locationsListFragmentContainer, leggTil);
        transaksjon.addToBackStack(null);

        // Legger ved hvilken destinasjon som ble valgt.
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
        transaksjon.replace(R.id.mapFragmentContainer, kart);

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
     * @return destinasjonsobjektet
     */
    public ArrayList<Destinasjon> getDestinasjoner()
    {
        return destinasjoner;
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
     * Setter destinasjonsobjektet og sorterer det etter enhetens posisjon.
     * @param destinasjoner
     */
    public void setDestinasjoner(ArrayList<Destinasjon> destinasjoner)
    {
        // Setter og sorterer ny tabell:
        this.destinasjoner = destinasjoner;
        sorterDestinasjoner();
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
            float[] resultater = new float[10];
            Location.distanceBetween(
                    getEnhetensPosisjon().latitude, getEnhetensPosisjon().longitude,
                    destinasjon.getKoordinat().latitude, destinasjon.getKoordinat().longitude,
                    resultater
            );

            destinasjon.setAvstandFraEnhet(resultater[0]);
            sorterbar[i++] = destinasjon; // Legger til elemeneter inn i en sorterbar tabell
        }

        // Sorterer tabellen:
        Arrays.sort(sorterbar);

        // Oppdaterer arraylist for visning:
        destinasjoner.clear();
        for( Destinasjon destinasjon : sorterbar )
            destinasjoner.add(destinasjon);

        // Oppdaterer view
        if (destinasjonsliste != null)
            destinasjonsliste.oppdaterListen();
    }

    /**
     * "Eventhandeler" for min lokasjon knappen. Flytter kartet til min
     * lokasjon.
     * @param view
     */
    public void gåTilLokasjon(View view)
    {
        kart.oppdaterBrukerPosisjon(getEnhetensPosisjon());
    }

    /**
     * "Eventhandeler" for legg til knapp.
     * @param view
     */
    public void leggTilLokasjon(View view)
    {
        FloatingActionButton fab = (FloatingActionButton)view;
        fab.setVisibility(View.GONE);

        skalerPanelVekting(0.8f);

        visLeggTilLokasjon();
    }

    public FloatingActionButton getLeggTilKnapp()
    {
        return leggTilKnapp;
    }

    //---------------------------------------------
    //  FØLGENDE ER KOPI FRA LEKSJON 12B.
    //  Modifisert for å passe applikasjonen.
    //---------------------------------------------

    public final static int REQUEST_LOCATION = 1;

    /**
     * Utføres når oppkobling skjer.
     * Direkte kopi fra leksjon 12b.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // OK: Appen har tillatelsen ACCESS_FINE_LOCATION. Finn siste posisjon
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null)
                this.setEnhetensPosisjon(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
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
