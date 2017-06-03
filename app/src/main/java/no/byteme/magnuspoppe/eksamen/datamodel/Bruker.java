package no.byteme.magnuspoppe.eksamen.datamodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Brukerklassen er til for å beskrive
 * en bruker. Kun en av de tre lagrede verdiene er
 * mulig å lagre i databasen. Dette er fordi jeg
 * ikke ville bruke tid på dette.
 * Created by MagnusPoppe on 29/05/2017.
 */
public class Bruker
{
    // Brukerens attributter:
    private String epost;
    private String fornavn;
    private String etternavn;

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

    /**
     * Sjekker om brukerens epost er formatert korrekt.
     * @return true hvis epost er korrekt formatert.
     */
    public boolean harKorrektEpost()
    {
        return validate(epost);
    }

    /**
     * @return epost adressen til brukeren, som den er.
     */
    public String getEpost()
    {
        return epost;
    }

    /**
     * @param epost til bruker
     */
    public void setEpost(String epost)
    {
        this.epost = epost;
    }

    /**
     * @return fornavnet til brukeren, som det er.
     */
    public String getFornavn()
    {
        return fornavn;
    }

    /**
     * @param fornavn på bruker
     */
    public void setFornavn(String fornavn)
    {
        this.fornavn = fornavn;
    }

    /**
     * @return etternavnet til brukeren, som det er.
     */
    public String getEtternavn()
    {
        return etternavn;
    }

    /**
     * @param etternavn på bruker
     */
    public void setEtternavn(String etternavn)
    {
        this.etternavn = etternavn;
    }
}
