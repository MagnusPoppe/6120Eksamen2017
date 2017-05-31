package no.byteme.magnuspoppe.eksamen;

import com.google.android.gms.maps.model.Marker;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

/**
 * Created by MagnusPoppe on 31/05/2017.
 */

public class MyMarker
{
    private String tittel;
    private Destinasjon destinasjon;

    public MyMarker(String tittel, Destinasjon destinasjon)
    {
        this.tittel = tittel;
        this.destinasjon = destinasjon;
    }

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
