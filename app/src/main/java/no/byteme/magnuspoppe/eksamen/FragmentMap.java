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
    MapFragment mapFragment;

    // Koordinater som vises:
    LatLng latLng;

    // Kartobjeketet for operasjoner mot kartet.
    GoogleMap map;

    final static private int DEFAULT_ZOOM = 14;

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
    public void onMapReady(GoogleMap map)
    {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    /**
     * Flytter kartets "layout" til oppgitte koordinater med fancy animasjon.
     * @param to koordinater å flytte til.
     */
    public void panTo(LatLng to)
    {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(to, DEFAULT_ZOOM));
    }

    /**
     * Markerer kartet med standard google maps markør.
     * @param title Tittelen på markøren
     * @param position posisjonen markøren skal plasseres på
     */
    public void markMap(String title, LatLng position)
    {
        map.addMarker(new MarkerOptions()
                .position(position)
                .draggable(false)
                .title(title)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_map, container, false);

        // Henter ut koordinater fra bundle:
        Bundle arguments = this.getArguments();

        if (arguments != null)
        {
            latLng = new LatLng(
                    arguments.getDouble(ActivityMain.MAIN_LAT),
                    arguments.getDouble(ActivityMain.MAIN_LNG)
            );
        }
        else
        {
            ActivityMain activity = (ActivityMain) getActivity();
            latLng = activity.getDevicePosition();
        }

        // Henter ut kartfragmentet
        mapFragment = (MapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return fragmentView;
    }
}
