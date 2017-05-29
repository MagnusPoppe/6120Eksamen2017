package no.byteme.magnuspoppe.eksamen.datamodel;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class Destination
{
    private int databaseID;
    private String name;
    private String type;
    private String description;
    private String imageURL;

    private double latitude;
    private double longitude;
    private double moh;

    public Destination(String JsonData)
    {

    }

    private LatLng getCoordinates()
    {
        return new LatLng(latitude, longitude);
    }
}
