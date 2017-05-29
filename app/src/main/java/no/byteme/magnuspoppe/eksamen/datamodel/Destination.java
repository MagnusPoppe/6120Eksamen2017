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
    private int moh;

    public Destination(String JsonData)
    {

    }

    public Destination(String name, String type, int moh, double lat, double lng)
    {
        this.name = name;
        this.type = type;
        this.latitude = lat;
        this.longitude = lng;
    }

    private LatLng getCoordinates()
    {
        return new LatLng(latitude, longitude);
    }

    public int getDatabaseID()
    {
        return databaseID;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getDescription()
    {
        return description;
    }

    public String getImageURL()
    {
        return imageURL;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public int getMoh()
    {
        return moh;
    }
}
