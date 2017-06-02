package no.byteme.magnuspoppe.eksamen.datamodel;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // REGEX mønster for å kjenne i igjen epost adresser:
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /**
     * Metode for å validere epostadresser. Denne og "Pattern" "VALID_EMAIL_ADDRESS_REGEX"
     * funnet hos StackOverflow:
     * https://stackoverflow.com/questions/8204680/java-regex-email
     * @param emailString
     * @return True hvis ekte post.
     */
    private static boolean validate(String emailString)
    {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailString);
        return matcher.find();
    }

    public boolean harKorrektEpost()
    {
        return validate(epost);
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
