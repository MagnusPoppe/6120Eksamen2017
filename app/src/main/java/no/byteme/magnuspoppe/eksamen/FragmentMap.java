package no.byteme.magnuspoppe.eksamen;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    final static private int STANDARD_ZOOM = 18;

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
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        this.googleKart = map;
        // Flytter kamera til rikitg initiell posisjon
        googleKart.animateCamera(CameraUpdateFactory.newLatLngZoom(koordinat, STANDARD_ZOOM));
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
     * @param tittel Tittelen på markøren
     * @param posisjon posisjonen markøren skal plasseres på
     */
    public void markerKartet(String tittel, LatLng posisjon)
    {
        googleKart.addMarker(new MarkerOptions()
                .position(posisjon)
                .draggable(false)
                .title(tittel)
        );
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
                    argumenter.getDouble(ActivityMain.HOVED_LATITIUDE),
                    argumenter.getDouble(ActivityMain.HOVED_LONGITUDE)
            );
            brukerEnhetPosisjon = false;
        }
        else
        {
            // Hvis ikke kordinater finnes, skal enhetens posisjon hentes ut.
            brukerEnhetPosisjon = true;
            ActivityMain aktivitet = (ActivityMain) getActivity();
            koordinat = aktivitet.getEnhetensPosisjon();
        }

        // Henter ut kartfragmentet
        map = (MapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);

        // Lager view:
        return view;
    }
}
