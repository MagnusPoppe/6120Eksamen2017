package no.byteme.magnuspoppe.eksamen;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import no.byteme.magnuspoppe.eksamen.datamodel.Bruker;
import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

/**
 * En klasse for å asynkront koble seg opp mot REST api
 * kjørende på tjeneren itfag.usn.no/~210852/
 *
 * Created by MagnusPoppe on 29/05/2017.
 */

public class AsynkronDestinasjon
{
    // URL for å koble seg opp:
    private final String GRUNN_URL = "http://itfag.usn.no/~210852/api.php/destination/";

    // Variabler for opplasting av lokalt lagrede destinasjoner:
    private boolean utførSlettingEtterOpplasting;
    private int[] skalSlettes;

    ActivityCtrl aktivitet;

    /**
     * Konstruktør for REST rammeverk:
     * @param activity
     */
    public AsynkronDestinasjon(ActivityCtrl activity)
    {
        this.aktivitet = activity;
        this.utførSlettingEtterOpplasting = false;
    }

    /**
     * Henter ut alle destinasjoner liggende i databasen.
     */
    public void get()
    {
        DestinasjonsOppgave oppgave = new DestinasjonsOppgave();
        oppgave.execute(GRUNN_URL, "GET");
    }

    /**
     * Henter ut alle destinasjonene for en gitt bruker:
     * @param bruker epost
     * @return
     */
    public ArrayList<Destinasjon> get( Bruker bruker )
    {
        return null; // NOT YET IMPLEMENTED
    }

    /**
     * Henter ut en gitt destinasjon med en gitt ID fra databasen
     * @param id i global database
     * @return
     */
    public ArrayList<Destinasjon> get(int id )
    {
        return null; // NOT YET IMPLEMENTED
    }

    /**
     * Lagrer et destinasjonsobjekt inn i databasen ved hjelp av POST.
     * Destinasjon konverteres til JSON før opplasting.
     * @param destinasjon som skal lagres.
     */
    public void post(Destinasjon destinasjon)
    {
        DestinasjonsOppgave oppgave = new DestinasjonsOppgave();
        oppgave.execute(GRUNN_URL, "POST", destinasjon.toJSON());
    }

    /**
     * Laster opp en gruppe med destinasjonsobjekter i en sending.
     * Dette krever at JSON objektene blir formatert inn i en JSON
     * array.
     * @param destinasjoner som skal sendes
     */
    public void post(Destinasjon[] destinasjoner)
    {
        // Bygger JSON streng:
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < destinasjoner.length; i++)
        {
            json.append(destinasjoner[i].toJSON());
            if (i < destinasjoner.length-1) json.append(",");
        }
        json.append("]");

        // Laster opp:
        DestinasjonsOppgave oppgave = new DestinasjonsOppgave();
        oppgave.execute(GRUNN_URL, "POST", json.toString());
    }

    /**
     * Spesial case, setter variabler for bruk av Callback
     * definert i "mellomLagerKontrakt" for å slette etter
     * komplett opplasting.
     * @param destinasjoner
     */
    public void postUopplastet(Destinasjon[] destinasjoner)
    {
        // Setter variabel for utføring av callback:
        utførSlettingEtterOpplasting = true;

        // Lagrer IDer på rader som skal slettes lokalt:
        skalSlettes = new int[destinasjoner.length];
        for (int i = 0; i < destinasjoner.length; i++)
        {
            skalSlettes[i] = destinasjoner[i].getDatabaseID();
        }

        // Utfører opplasting:
        post(destinasjoner);
    }

    /**
     * Den asynkrone opplastingsklassen. Denne laster opp elementer med HTTP
     * metode. Den kan sende med data også. Her følger jeg hovedsakelig
     * mønsteret vist i forelesning, men med en vri jeg lærte fra et sett
     * med videoer på YouTube, publisert av Udacity i samarbeid med Google:
     * https://www.youtube.com/playlist?list=PLAwxTw4SYaPmXoZF20ky_Amke3Mu4lZEo
     *
     * Her sender jeg med de nødvendige parameterene som Strenger. Man kan ha
     * et ubestemt antall parametere til "exectute()" metoden til AsyncTask.
     * Parametere:
     * 0: URL til opplastingsplassering
     * 1: HTTP METODE som skal brukes
     * 2: Pakken som skal lastes opp i JSON format:
     */
    private class DestinasjonsOppgave extends AsyncTask<String, Void, Long>
    {

        // PARAMS KONSTANTER:
        final static int URL = 0;
        final static int HTTP_METODE = 1;
        final static int PAKKE = 2;

        // Statuscodes:
        final static long FEIL = 0l;
        final static long OK = 1l;
        final static long JSON_ANALYSE_FEIL = 3l;
        final static long IO_FEIL = 4l;
        final static long FEILFORMATERT_URL_FEIL = 5l;

        /**
         * Bakgrunnsoppgaven som utfører opplastingen til tjeneren:
         * @param params parametere som legges ved i sendingen
         *               0: URL til opplastingsplassering
         *               1: HTTP METODE som skal brukes
         *               2: Pakken som skal lastes opp i JSON format, Tom hvis ingen sending
         * @return statuskode
         */
        @Override
        protected Long doInBackground(String... params)
        {
            HttpURLConnection oppkobling = null;
            long resultat = 0L;

            try
            {
                // Setter opp koblingen:
                java.net.URL url = new URL(params[URL]);
                oppkobling = (HttpURLConnection) url.openConnection();
                oppkobling.setRequestMethod(params[HTTP_METODE]);

                // Denne gjelder egentlig bare "PUT" og "POST" men
                // er med alikevel.
                oppkobling.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // Hvis PUT / POST / DELETE metoden er i bruk skal man sende med data.
                if (params[HTTP_METODE].equals("PUT")
                        || params[HTTP_METODE].equals("POST")
                        || params[HTTP_METODE].equals("DELETE"))
                {
                    // For å motta data tilbake. Denne kan ikke settes for begge
                    // utefor IF av ukjente oversaker.
                    oppkobling.setDoInput(true);

                    // oppsett for å sende med data:
                    oppkobling.setDoOutput(true);
                    oppkobling.setChunkedStreamingMode(0);
                    oppkobling.setRequestProperty("Transfer-Encoding", "chunked");
                }
                // Elles skal man fortsatt motta data:
                else {
                    oppkobling.setDoInput(true);
                }

                // Kobler opp:
                oppkobling.connect();

                // Hvis PUT / POST / DELETE metoden er i bruk skal man sende med data.
                if (params[HTTP_METODE].equals("PUT")
                        || params[HTTP_METODE].equals("POST")
                        || params[HTTP_METODE].equals("DELETE"))
                {
                    // Utfører sendingen av data:
                    OutputStreamWriter ut = new OutputStreamWriter(oppkobling.getOutputStream());
                    ut.write(params[PAKKE]);
                    ut.close();
                }

                // Sjekker statuskoden for feil. Et REST API skal alltid levere
                // tilbake en fornuftig statuskode. Dette gjeler også mitt API:
                int respons = oppkobling.getResponseCode();
                if (respons != HttpURLConnection.HTTP_OK)
                    return FEIL;

                // Ved GET forespørsel skal vi ha tilbake data:
                if (params[HTTP_METODE].equals("GET"))
                {
                    // Lager innlesingsobjekt:
                    BufferedReader innleser = new BufferedReader(new InputStreamReader(
                            oppkobling.getInputStream())
                    );

                    // Bruker string builder for raskere oppbygging av JSON svaret:
                    StringBuilder pakke = new StringBuilder();
                    String linje;

                    // Leser inn data:
                    while ((linje = innleser.readLine()) != null)
                        pakke.append(linje);

                    // Analyse (PARSE) og gjennomgang av JSON:
                    analyserData(pakke.toString());
                }
                // Sender tilbake at alt er OK!
                resultat = OK;

            }
            // CATCH FRO FORSKJELLIGE FEILMELDINGER:
            catch (MalformedURLException e)
            {
                Log.e("ASYNC FEIL", aktivitet.getResources().getString(R.string.error) + e);
                resultat = FEILFORMATERT_URL_FEIL;
            } catch (IOException e)
            {
                Log.e("ASYNC FEIL", aktivitet.getResources().getString(R.string.error) + e);
                resultat = IO_FEIL;
            } catch (JSONException e)
            {
                Log.e("ASYNC FEIL", aktivitet.getResources().getString(R.string.error) + e);
                resultat = JSON_ANALYSE_FEIL;
            } finally
            {
                // Tilslutt skal det uansett kobles ned:
                if (oppkobling != null)
                    oppkobling.disconnect();
            }
            return resultat;
        }

        /**
         * Lager objekter ut av de mottatte dataene. Dataene blir også sortert.
         * Jeg har valgt å gjøre dette i bakgrunn grunnet at det er en tung prosess som
         * ikke er ønsket på UI tråden.
         *
         * @param json data gotten from the traffic branch.
         * @throws JSONException
         */
        private void analyserData(String json) throws JSONException
        {
            // Tolker dataene
            JSONArray tabell = new JSONArray(json);

            // Lager en ut liste
            ArrayList<Destinasjon> destinasjoner = new ArrayList<>();

            for (int i = 0; i < tabell.length(); i++)
            {
                destinasjoner.add(new Destinasjon( (JSONObject) tabell.get(i)));
            }

            // Setter på og sorterer:
            aktivitet.setDestinasjoner(destinasjoner);
        }

        /**
         * "onPostExecute" lagrer kun en statusmelding. Dette blir logget i
         * android enheten.
         * @param resultat av oppkobling
         */
        @Override
        protected void onPostExecute(Long resultat)
        {
            super.onPostExecute(resultat);

            String status = "";

            if (resultat == OK)
            {
                status = "Executed rest call successfully.";

                // Utfører callback hvis nødvendig:
                if (utførSlettingEtterOpplasting)
                    aktivitet.vedKomplettOpplastingAvDestinasjoner(skalSlettes);

                // Oppdaterer uansett listen hvis nødvending:
                aktivitet.oppdaterDestnasjonsliste();

            } else if (resultat == FEILFORMATERT_URL_FEIL)
            {
                status = "Bad url...";
            } else if (resultat == IO_FEIL)
            {
                status = "Problems reading the downloaded data. ( I/O FEIL )";
            } else if (resultat == JSON_ANALYSE_FEIL)
            {
                status = "Bad JSON format on data.";
            } else if (resultat == FEIL)
            {
                status = "Something went wrong.";
            } else
            {
                status = "Unknown errorcode... WHAT?!";
            }

            // Logger resultat:
            Log.d("ASYNC_POST_EXECUTE", "Result-code=" + resultat + ", Message=" + status);
        }
    }
}
