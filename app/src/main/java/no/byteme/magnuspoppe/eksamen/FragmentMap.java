package no.byteme.magnuspoppe.eksamen;


import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

/**
 * Dette fragmentet inneholder kartet som vises på
 * forsiden av appen. Dette er det mest viste fragmentet
 * av alle fragmentene.
 *
 * Jeg valgte å lage mitt eget fragment over "MapFragment"
 * fragmentet til google. Dette er for å få egen
 * funksjonalitet oppå google sin funksjonalitet.
 */
public class FragmentMap extends Fragment implements OnMapReadyCallback
{
    // Mellomlagring av Posisjonselementer:
    private static final String ENHETPOS = "brukerEnhetPosisjon";
    private static final String CAM = "CAMERA";

    // Google sitt fragment for kart:
    MapFragment kart;

    // Koordinater som vises:
    LatLng koordinat;

    // Kartobjeketet for operasjoner mot kartet.
    GoogleMap googleKart;
    CameraPosition kamera;

    // Liste for markøerer lagt ut på kartet:
    ArrayList<MyMarker> markorer;

    // Standarder for zooming:
    final static private int STANDARD_ZOOM = 16;
    final static private int YTRE_ZOOM = 10;

    // Bool for å vite om man bruker enhetens posisjon:
    boolean brukerEnhetPosisjon;

    public FragmentMap()
    {
        // Required empty public constructor
    }

    /**
     * Lagrer data nødvendig for å vise kartet slik som det sist så.
     * Denne lagrer "CameraPosition" objekt for mest mulig fullstendige
     * data om kameraet.
     * @param outState bundle som blir lagret.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putParcelable(CAM, googleKart.getCameraPosition());
        outState.putBoolean(ENHETPOS, brukerEnhetPosisjon);
        super.onSaveInstanceState(outState);
    }

    /**
     * onMapReady lastes inn da kartet er ferdig med sin asynkrone oppgave.
     * Dette er oppsettet av kartet.
     * @param map
     */
    @Override
    public void onMapReady(GoogleMap map)
    {
        // Lagerer kartet globalt for operasjoner mot det:
        this.googleKart = map;

        // Lagrer og velger utseende:
        googleKart.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // Tillatelsen er allerede hentet på dette punktet.
        // Try/catch for å ikke støte på feilmelidnger.
        try {
            googleKart.setMyLocationEnabled(true);
            googleKart.getUiSettings().setMyLocationButtonEnabled(false); // Denne er på menu.
        }
        catch (SecurityException e)
        {
            Snackbar.make(
                    getView(),
                    getResources().getString(R.string.manglerTillatelsePos),
                    Snackbar.LENGTH_SHORT
            ).show();
        }

        // Flytter kamera til rikitg initiell posisjon
        if (this.kamera != null) // Hvis gjenopprettet:
        {
            googleKart.moveCamera(CameraUpdateFactory.newCameraPosition(kamera));
        }
        else // hvis første start:
        {
            googleKart.moveCamera(CameraUpdateFactory.newLatLngZoom(koordinat, YTRE_ZOOM));
        }

        // Henter kontroller:
        final ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
        aktivitet.settUtAlleMarkorer();

        // Lager lyttere på nyttige ting:
        googleKart.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                // Denne lytteren bruker egenlagd klasse for Markører
                // for å benytte meg av equals metode for å oppdage korrekt
                // markør:
                for (MyMarker markor : markorer)
                {
                    if (markor.equals(marker))
                    {
                        aktivitet.visDetaljertInformasjonsPanel(markor.getDestinasjon());
                        marker.showInfoWindow();
                        flyttKameraTil(markor.getDestinasjon().getKoordinat());
                        return true;
                    }
                }
                return false;
            }
        });
        googleKart.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener()
        {
            @Override
            public void onCameraMove()
            {
                brukerEnhetPosisjon = false;
            }
        });
    }

    /**
     * Enkel boolean for å bestemme om man får lov til å styre kartet
     * eller ikke.
     * @param paa om kartet skal kunne brukes. True betyr ja.
     */
    public void kanInteragere(Boolean paa)
    {
        googleKart.getUiSettings().setAllGesturesEnabled(paa);
    }

    /**
     * Flytter kartets "layout" til oppgitte koordinater med fancy animasjon.
     * @param nyPosisjon koordinater å flytte til.
     */
    public void oppdaterBrukerPosisjon(LatLng nyPosisjon)
    {
        brukerEnhetPosisjon = true;
        googleKart.animateCamera(CameraUpdateFactory.newLatLngZoom(nyPosisjon, STANDARD_ZOOM));
    }

    /**
     * Flytter kartets "layout" til oppgitte koordinater med fancy animasjon.
     * @param til koordinater å flytte til.
     */
    public void flyttKameraTil(LatLng til)
    {
        brukerEnhetPosisjon = false;
        googleKart.animateCamera(CameraUpdateFactory.newLatLngZoom(til, STANDARD_ZOOM));
    }

    /**
     * Markerer kartet med standard google maps markør.
     * @param destinasjon som skal plasseres ut
     * @param posisjon posisjonen markøren skal plasseres på
     */
    public void markerKartet(Destinasjon destinasjon, LatLng posisjon)
    {
        if (googleKart != null)
        {
            // Plasserer markor på kartet
            googleKart.addMarker(new MarkerOptions()
                    .position(posisjon)
                    .draggable(false)
                    .title(destinasjon.getNavn())
            );
            // Lagrer i listen for gjenoppdaging:
            markorer.add(new MyMarker(destinasjon.getNavn(),destinasjon));
        }
    }

    /**
     * Fjerner alle markører på kartet og tømmer markør listen:
     */
    public void fjernAlleMarkorer()
    {
        if (googleKart != null)
            googleKart.clear();

        if (markorer != null)
            markorer.clear();
    }

    /**
     * Lager selve viewet:
     * @param inflater  Blåser opp layout
     * @param container Holder på view:
     * @param savedInstanceState lagrede data
     * @return ferdig konfigurert fragment view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Henter ut koordinater fra bundle:
        Bundle argumenter = this.getArguments();

        // Liste av alle markører på kartet:
        markorer = new ArrayList<>();

        // OM STATE FOR KARTFRAGMENTET:
        // Henter ut koordinater. Dette tilfellet av å beholde "STATE" for
        // fragmentet var vanskelig. Det er ikke mulig å lagre "MapFragment"
        // fordi, av en eller annen merkelig grunn får jeg ikke typetivnget den?
        // Også, kan heller ikke lagre den som en del av "FragmentMap" fragmentet.
        // Løsningen var derfor å beholde alle data om fragmentet, så lage det på
        // nytt. Så likt som mulig det forrige "bildet" av kartet.
        if (savedInstanceState != null) // Fant mellomlagret data om kartet:
        {
            if (savedInstanceState.containsKey(CAM))
            {
                kamera = savedInstanceState.getParcelable(CAM);
            }
            if (savedInstanceState.containsKey(ENHETPOS))
                brukerEnhetPosisjon = savedInstanceState.getBoolean(ENHETPOS);
        }
        else // Ingen mellomlagret data:
        {
            if (argumenter != null) // Det ble sendt med koordinater for visning av kart:
            {
                // Hvis koordinater finnes, skal de lagres.
                koordinat = new LatLng(
                        argumenter.getDouble(ActivityCtrl.HOVED_LATITIUDE),
                        argumenter.getDouble(ActivityCtrl.HOVED_LONGITUDE)
                );
                brukerEnhetPosisjon = false;
            }
            else // Ingen koordinater.
            {
                // Hvis ikke kordinater finnes, skal enhetens posisjon hentes ut.
                brukerEnhetPosisjon = true;
                ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
                koordinat = aktivitet.getEnhetensPosisjon();
            }
        }

        // Lager kartfragmentet til google med dataene vi har. Dette gjøres asynkront:
        kart = (MapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        kart.getMapAsync(this);

        // Lager view:
        return view;
    }

    /**
     * @return sant hvis enhetens posisjon er i bruk, eller usant.
     */
    public boolean isBrukerEnhetPosisjon()
    {
        return brukerEnhetPosisjon;
    }
}
