package no.byteme.magnuspoppe.eksamen.datamodel;

import java.util.ArrayList;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class Bruker
{
    // Brukerens attributter:
    private String epost;
    private String fornavn;
    private String etternavn;

    ArrayList<Destinasjon> userLocations;

    public Bruker(String epost, String fornavn, String etternavn)
    {
        this.epost = epost;
        this.fornavn = fornavn;
        this.etternavn = etternavn;
    }

    public String getEpost()
    {
        return epost;
    }

    public void setEpost(String epost)
    {
        this.epost = epost;
    }

    public String getFornavn()
    {
        return fornavn;
    }

    public void setFornavn(String fornavn)
    {
        this.fornavn = fornavn;
    }

    public String getEtternavn()
    {
        return etternavn;
    }

    public void setEtternavn(String etternavn)
    {
        this.etternavn = etternavn;
    }

}
