package no.byteme.magnuspoppe.eksamen.datamodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class Destinasjon implements Comparable, Parcelable
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

    private boolean iGlobalDatabase;

    private float avstandFraEnhet;

    /**
     * Konstruktør brukt når man legger til ny lokasjon.
     * @param moh Meter over havet
     * @param lat latitude (del av koordinat)
     * @param lng longitude (del av koordinat)
     */
    public Destinasjon(double moh, double lat, double lng)
    {
        this.latitude = lat;
        this.longitude = lng;
        this.moh = (int) moh;
        this.iGlobalDatabase = false;
    }

    public Destinasjon(int ID, String eier, String navn, String type, String beskrivelse,
                       String bildeURL, double latitude, double longitude, int moh
    ) {
        this.databaseID = ID;
        this.eier = eier;
        this.navn = navn;
        this.type = type;
        this.beskrivelse = beskrivelse;
        this.bildeURL = bildeURL;
        this.latitude = latitude;
        this.longitude = longitude;
        this.moh = moh;
        this.iGlobalDatabase = false;
    }

    /**
     * Lager et destinajsonsobjekt ut ifra et JSONObject.
     * Objektet er ferdig formatert fra API og skal ha strukturen:
     * {
     *   "locationID": "1",
     *   "owner": "jon@kvisli.no",
     *   "name": "Gygrestolen",
     *   "type": "Utsiktspunkt",
     *   "description": "En fottur (...) fjellvann som ypperlig for en dukkert. ",
     *   "imageURL": "http://adr.no/bilde.jpg",
     *   "lat": "59.3660128",
     *   "lng": "8.9787209",
     *   "moh": "490"
     * }
     *
     * Når en null verdi blir konvertert fra databasen til JSON objekt gjennom
     * PHP blir null verdien gjort om til en "null" streng. Dette ryddes opp
     * i tilslutt, i feltene dette gjelder.
     *
     * @see JSONObject
     * @param JsonData
     */
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

        // Rydder opp i dataene som ikke var komplette fra databasen:
        if (navn.equals("null")) navn = "";
        if (type.equals("null")) type = "";
        if (beskrivelse.equals("null")) beskrivelse = "";
        if (bildeURL.equals("null")) bildeURL = "";

        // Disse kommer fra nett
        iGlobalDatabase = true;
    }

    public Destinasjon(Parcel in)
    {
        databaseID = in.readInt();
        eier = in.readString();
        navn = in.readString();
        type = in.readString();
        beskrivelse = in.readString();
        bildeURL = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        moh = in.readInt();
    }

    @Override
    public int compareTo(@NonNull Object o)
    {
        Destinasjon andre = (Destinasjon) o;
        return Math.round(getAvstandFraEnhet() - andre.getAvstandFraEnhet());
    }

    public boolean isiGlobalDatabase()
    {
        return iGlobalDatabase;
    }

    public void setiGlobalDatabase(boolean iGlobalDatabase)
    {
        this.iGlobalDatabase = iGlobalDatabase;
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

    public void setNavn(String navn)
    {
        this.navn = navn;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setBeskrivelse(String beskrivelse)
    {
        this.beskrivelse = beskrivelse;
    }

    /**
     * Lager en streng med JSON notasjon versjon av objektet. Dette
     * skal brukes med database håndtering.
     *
     * @return JSON formatert objekt.
     */
    public String toJSON()
    {
        return "{" +
                "\"locationID\":\"" + getDatabaseID() + "\"," +
                "\"owner\":\"" + getEier() + "\"," +
                "\"name\":\"" + getNavn() + "\"," +
                "\"type\":\"" + getType() + "\"," +
                "\"description\":\"" + getBeskrivelse() + "\"," +
                "\"imageURL\":\"" + getBildeURL() + "\"," +
                "\"lat\":" + getKoordinat().latitude + "," +
                "\"lng\":" + getKoordinat().longitude + "," +
                "\"moh\":" + getMoh() +
                "}";
    }

    public void setEier(String eier)
    {
        this.eier = eier;
    }

    public void setDatabaseID(int databaseID)
    {
        this.databaseID = databaseID;
    }

    public void setBildeURL(String bildeURL)
    {
        this.bildeURL = bildeURL;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(databaseID);
        dest.writeString(eier);
        dest.writeString(navn);
        dest.writeString(type);
        dest.writeString(beskrivelse);
        dest.writeString(bildeURL);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(moh);
        dest.writeBooleanArray(new boolean[]{isiGlobalDatabase()});
    }

    /**
     * Mønster på hvordan lagre ArrayList<> av generisk type til parcel for
     * at objektene skal overleve rotasjon.
     *
     * Lenke til inspirasjonskilde:
     * https://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
     */
    public static final Parcelable.Creator<Destinasjon> CREATOR = new Parcelable.Creator<Destinasjon>() {
        public Destinasjon createFromParcel(Parcel in) {
            return new Destinasjon(in);
        }

        public Destinasjon[] newArray(int size) {
            return new Destinasjon[size];
        }
    };
}
