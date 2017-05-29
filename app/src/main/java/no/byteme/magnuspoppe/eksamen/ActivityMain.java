package no.byteme.magnuspoppe.eksamen;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

public class ActivityMain extends Activity
{

    private static final LatLng HØYSKOLEN = new LatLng(59.408852, 9.059512);
    private static final LatLng GYGRESTOLEN = new LatLng(59.367091, 8.976251);

    public static final String MAIN_LAT = "kldaføjsefølakjdf";
    public static final String MAIN_LNG = "asløkdsalskdjfkal";

    private LatLng devicePosition;
    FragmentMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicePosition = HØYSKOLEN;
        displayMap();
    }

    /**
     * Viser kartet uten parametere. Dette vil gjøre at
     * kartfragmentet (FragmentMap) vil gjøre sin standard
     * visning av enhetens koordinater.
     */
    private void displayMap()
    {
        displayMap(null);
    }

    /**
     * Viser kartet som fragment inn i "mapFragmentContainer"
     * @param coordinates koordinater kartet skal sentrere seg rundt.
     */
    private void displayMap(LatLng coordinates)
    {
        // Henter nødvendige data
        map = new FragmentMap();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mapFragmentContainer, map);

        // Legger ved koordinater som skal vises i kartet:
        if (coordinates != null)
        {
            Bundle item = new Bundle();
            item.putDouble(MAIN_LAT, coordinates.latitude);
            item.putDouble(MAIN_LNG, coordinates.longitude);
            map.setArguments(item);
            transaction.addToBackStack(null);
        }

        // Utfører transaksjonen.
        transaction.commit();
    }

    /**
     * @return Enhetens lokasjon
     */
    public LatLng getDevicePosition()
    {
        return devicePosition;
    }
}
