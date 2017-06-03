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
 * forsiden av appen.
 */
public class FragmentMap extends Fragment implements OnMapReadyCallback
{
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

    final static private int STANDARD_ZOOM = 16;
    final static private int YTRE_ZOOM = 10;

    public boolean isBrukerEnhetPosisjon()
    {
        return brukerEnhetPosisjon;
    }

    boolean brukerEnhetPosisjon;

    public FragmentMap()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putParcelable(CAM, googleKart.getCameraPosition());
        outState.putBoolean(ENHETPOS, brukerEnhetPosisjon);
        super.onSaveInstanceState(outState);
    }

//    @Override
//    public void onViewStateRestored(Bundle savedInstanceState)
//    {
//        super.onViewStateRestored(savedInstanceState);
//        if (savedInstanceState != null && savedInstanceState.containsKey(CAM))
//        {
//            kamera = savedInstanceState.getParcelable(CAM);
//        }
//    }

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
        // Try catch for å ikke støte på feilmelidnger.
        try {
            googleKart.setMyLocationEnabled(true);
            googleKart.getUiSettings().setMyLocationButtonEnabled(false); // Denne er på menubar.
        }
        catch (SecurityException e)
        {
            Snackbar.make(
                    getView(),
                    "Har ikke tillatelse til å bruke lokasjon.",
                    Snackbar.LENGTH_SHORT
            ).show();
        }

        // Flytter kamera til rikitg initiell posisjon
        if (this.kamera != null)
        {
            googleKart.moveCamera(CameraUpdateFactory.newCameraPosition(kamera));
        }
        else
        {
            googleKart.moveCamera(CameraUpdateFactory.newLatLngZoom(koordinat, YTRE_ZOOM));
        }

        final ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
        aktivitet.settUtAlleMarkorer();

        // Lager lyttere på nyttige ting:
        googleKart.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
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
            googleKart.addMarker(new MarkerOptions()
                    .position(posisjon)
                    .draggable(false)
                    .title(destinasjon.getNavn())
            );
            markorer.add(new MyMarker(destinasjon.getNavn(),destinasjon));
        }
    }

    public void fjernAlleMarkorer()
    {
        if (googleKart != null)
            googleKart.clear();

        if (markorer != null)
            markorer.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Henter ut koordinater fra bundle:
        Bundle argumenter = this.getArguments();

        markorer = new ArrayList<>();

        // <b>OM STATE FOR KARTFRAGMENTET<b>:
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
}
