package no.byteme.magnuspoppe.eksamen;

import com.google.android.gms.maps.model.Marker;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

/**
 * Dette er en enkel hjelpeklasse for å sammenlikne destinasjoner med
 * markører på kartet. Dette er viktig for å få til samspillet mellom klikk på
 * kartet og det grafiske i Appen.
 *
 * Created by MagnusPoppe on 31/05/2017.
 */
public class MyMarker
{
    // Nødvendige data for sammenlikning og bruk:
    private String tittel;
    private Destinasjon destinasjon;

    /**
     * Enkel konstruktør
     * @param tittel på markør
     * @param destinasjon som ører med.
     */
    public MyMarker(String tittel, Destinasjon destinasjon)
    {
        this.tittel = tittel;
        this.destinasjon = destinasjon;
    }

    /**
     * Denne egenskapte equals metoden sammenlikner navnet på
     * markøren og destinasjonens navn.
     *
     * Grunnen til at klassen finnes:
     * Det er ikke mulig å arve av "marker" klassen. Løsningen ble derfor
     * denne metoden.
     * @param obj av typen Marker eller typen MyMarker
     * @return sant hvis samme objekt.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Marker)
        {
            Marker andre = (Marker) obj;
            return tittel.equals(andre.getTitle());
        }
        else if (obj instanceof MyMarker)
        {
            MyMarker andre = (MyMarker) obj;
            return tittel.equals(andre.getTittel());
        }
        // No match:
        return false;
    }

    public String getTittel()
    {
        return tittel;
    }

    public void setTittel(String tittel)
    {
        this.tittel = tittel;
    }

    public Destinasjon getDestinasjon()
    {
        return destinasjon;
    }

    public void setDestinasjon(Destinasjon destinasjon)
    {
        this.destinasjon = destinasjon;
    }
}
