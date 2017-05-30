package no.byteme.magnuspoppe.eksamen;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.LinearLayout;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

public class ActivityMain extends Activity
{
    private static final LatLng HØYSKOLEN = new LatLng(59.408852, 9.059512);

    public static final String HOVED_LATITIUDE = "kldaføjsefølakjdf";
    public static final String HOVED_LONGITUDE = "asløkdsalskdjfkal";

    private LatLng enhetensPosisjon;
    FragmentMap kart;
    FragmentCloseLocationList destinasjonsliste;

    LinearLayout bunnPanel;
    LinearLayout kartPanel;

    private ArrayList<Destinasjon> destinasjoner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Henter ut destinasjonsdata asynkront:
        destinasjoner = new ArrayList<>();
        AsynkronDestinasjon oppgave = new AsynkronDestinasjon(this);
        oppgave.get();

        // Lager kart og listepanel:
        enhetensPosisjon = HØYSKOLEN;
        visKart();
        visDestinasjonsListe();

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

        LocationManager lokasjonsstyrer = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener lokasjonslytter = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                setEnhetensPosisjon(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lokasjonsstyrer.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, lokasjonslytter);
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
        transaksjon.addToBackStack(null);

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
        return enhetensPosisjon;
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
                    enhetensPosisjon.latitude, enhetensPosisjon.longitude,
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

}
