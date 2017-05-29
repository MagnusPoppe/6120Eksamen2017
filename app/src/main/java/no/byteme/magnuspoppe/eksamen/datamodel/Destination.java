package no.byteme.magnuspoppe.eksamen.datamodel;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class Destination implements Comparable
{
    private int databaseID;
    private String owner;
    private String name;
    private String type;
    private String description;
    private String imageURL;

    private double latitude;
    private double longitude;
    private int moh;

    private float distanceFromDevice;

    public Destination(JSONObject JsonData)
    {
        databaseID = JsonData.optInt("locationID");
        owner = JsonData.optString("owner");
        name = JsonData.optString("name");
        type = JsonData.optString("type");
        description = JsonData.optString("description");
        imageURL = JsonData.optString("imageURL");
        latitude = JsonData.optDouble("lat");
        longitude = JsonData.optDouble("lng");
        moh = JsonData.optInt("moh");
    }

    public Destination(String name, String type, int moh, double lat, double lng)
    {
        this.name = name;
        this.type = type;
        this.moh = moh;
        this.latitude = lat;
        this.longitude = lng;
    }

    public LatLng getCoordinates()
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

    public int getMoh()
    {
        return moh;
    }

    public float getDistanceFromDevice()
    {
        return distanceFromDevice;
    }

    public void setDistanceFromDevice(float distanceFromDevice)
    {
        this.distanceFromDevice = distanceFromDevice;
    }

    @Override
    public int compareTo(@NonNull Object o)
    {
        Destination other = (Destination) o;
        return Math.round(getDistanceFromDevice() - other.getDistanceFromDevice());
    }

    public String getOwner()
    {
        return owner;
    }
}
