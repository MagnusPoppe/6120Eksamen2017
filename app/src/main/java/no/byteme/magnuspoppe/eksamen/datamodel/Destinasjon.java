package no.byteme.magnuspoppe.eksamen.datamodel;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class Destinasjon implements Comparable
{
    private int databaseID;
    private String eier;
    private String navn;
    private String type;
    private String beskrivelse;
    private String bildeURL;

    private double latitude;
    private double longitude;
    private int moh;

    private float avstandFraEnhet;

    public Destinasjon(JSONObject JsonData)
    {
        databaseID = JsonData.optInt("locationID");
        eier = JsonData.optString("owner");
        navn = JsonData.optString("name");
        type = JsonData.optString("type");
        beskrivelse = JsonData.optString("description");
        bildeURL = JsonData.optString("imageURL");
        latitude = JsonData.optDouble("lat");
        longitude = JsonData.optDouble("lng");
        moh = JsonData.optInt("moh");
    }

    @Override
    public int compareTo(@NonNull Object o)
    {
        Destinasjon andre = (Destinasjon) o;
        return Math.round(getAvstandFraEnhet() - andre.getAvstandFraEnhet());
    }

    public LatLng getKoordinat()
    {
        return new LatLng(latitude, longitude);
    }

    public int getDatabaseID()
    {
        return databaseID;
    }

    public String getNavn()
    {
        return navn;
    }

    public String getType()
    {
        return type;
    }

    public String getBeskrivelse()
    {
        return beskrivelse;
    }

    public String getBildeURL()
    {
        return bildeURL;
    }

    public int getMoh()
    {
        return moh;
    }

    public float getAvstandFraEnhet()
    {
        return avstandFraEnhet;
    }

    public void setAvstandFraEnhet(float avstandFraEnhet)
    {
        this.avstandFraEnhet = avstandFraEnhet;
    }

    public String getEier()
    {
        return eier;
    }
}
