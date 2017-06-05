package no.byteme.magnuspoppe.eksamen.datamodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Denne klassen holder på dataobjektetene til alle turmål.
 *
 * Grunnen til at klassen ble kalt "Destinasjon" var at jeg
 * først begynte programmeringen på engelsk, og dermed endte
 * opp med at oversettelsen til Turmål var destinasjon. Når
 * jeg så at eksamen skulle skrives på norsk ble den refaktorert
 * slik. Jeg vil helst unngå Æ-Ø-Å i koden.
 *
 * Created by MagnusPoppe on 29/05/2017.
 */

public class Destinasjon implements Comparable, Parcelable
{
    // Data om sted:
    private int databaseID;
    private String eier;
    private String navn;
    private String type;
    private String beskrivelse;
    private String bildeURL;

    // Posisjonsrelaterte data:
    private double latitude;
    private double longitude;
    private int moh;
    private float avstandFraEnhet;

    // Boolean på å se om objektet er lastet opp i database:
    private boolean iGlobalDatabase;

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

    /**
     * Konstruktør med alle felter. Brukes av DestinasjonDB
     * @param ID til global database hvis lastet opp, ellers lokal.
     * @param eier av turmålet
     * @param navn på turmålet
     * @param type av turmålet
     * @param beskrivelse på turmålet
     * @param bildeURL til turmålet
     * @param latitude for turmålet
     * @param longitude for turmålet
     * @param moh ved turmålets lokasjon.
     */
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

    /**
     * Konstruktør for å tillate mellomlagring av objektet som
     * Parcelable. Dette ble lagt inn for å kunne lagre hele
     * datasettet i "savedInstaceState".
     * @param in objekt som skal dekodes.
     */
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
    public int describeContents()
    {
        return 0;
    }

    /**
     * Lagerer objektet i parcel format for å kunne mellomlagre på rotasjon og
     * ved andre hedelser.
     * @param dest
     * @param flags
     */
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
    /**
     * Sammenlikner avstanden mellom to destinasjoner. Brukes
     * ved sortering av listeelementer for nærmeste først.
     * @param o objeket
     * @return
     */
    @Override
    public int compareTo(@NonNull Object o)
    {
        Destinasjon andre = (Destinasjon) o;
        return Math.round(getAvstandFraEnhet() - andre.getAvstandFraEnhet());
    }

    /**
     * Lager en streng med JSON notasjon versjon av objektet. Dette
     * skal brukes med database håndtering.
     *
     * Jeg fikk beskjed i obligatorisk innlevering å lage en
     * "char fnutt = '"';" istedenfor å bruke "escape" tegn.
     * Jeg mener dette er en feil, siden sammenslåing av streng
     * er tyngre enn å bruke "escape" tegn i enkelte språk, og
     * jeg personlig mener dette er mer ryddig, pent og lesbart.
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

    // FØLGENDE ER KUN GETTERS OG SETTERS:

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
}
