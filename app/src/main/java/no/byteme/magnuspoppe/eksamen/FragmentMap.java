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
    // Google sitt fragment for kart:
    MapFragment map;

    // Koordinater som vises:
    LatLng koordinat;

    // Kartobjeketet for operasjoner mot kartet.
    GoogleMap googleKart;
    GoogleMap.OnMarkerClickListener listener;

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

    /**
     * onMapReady lastes inn da kartet er ferdig med sin asynkrone oppgave.
     * Dette er oppsettet av kartet.
     * @param map
     */
    @Override
    public void onMapReady(GoogleMap map)
    {
        // Lagrer og velger utseende:
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // Tillatelsen er allerede hentet på dette punktet.
        // Try catch for å ikke støte på feilmelidnger.
        try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
        catch (SecurityException e)
        {
            Snackbar.make(
                    getView(),
                    "Har ikke tillatelse til å bruke lokasjon.",
                    Snackbar.LENGTH_SHORT
            ).show();
        }

        this.googleKart = map;
        // Flytter kamera til rikitg initiell posisjon
        googleKart.animateCamera(CameraUpdateFactory.newLatLngZoom(koordinat, YTRE_ZOOM));

        final ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
        aktivitet.settUtAlleMarkorer();

        listener = new GoogleMap.OnMarkerClickListener()
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
        };
        googleKart.setOnMarkerClickListener(listener);
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

        if (argumenter != null)
        {
            // Hvis koordinater finnes, skal de lagres.
            koordinat = new LatLng(
                    argumenter.getDouble(ActivityCtrl.HOVED_LATITIUDE),
                    argumenter.getDouble(ActivityCtrl.HOVED_LONGITUDE)
            );
            brukerEnhetPosisjon = false;
        }
        else
        {
            // Hvis ikke kordinater finnes, skal enhetens posisjon hentes ut.
            brukerEnhetPosisjon = true;
            ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
            koordinat = aktivitet.getEnhetensPosisjon();
        }

        markorer = new ArrayList<>();

        // Henter ut kartfragmentet
        map = (MapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);

        // Lager view:
        return view;
    }
}
