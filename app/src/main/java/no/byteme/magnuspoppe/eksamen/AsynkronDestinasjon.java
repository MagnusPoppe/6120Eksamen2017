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

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class AsynkronDestinasjon
{
    private final String GRUNN_URL = "http://itfag.usn.no/~210852/api.php/destination/";
    ActivityMain aktivitet;

    public AsynkronDestinasjon(ActivityMain activity)
    {
        this.aktivitet = activity;
    }

    public void get()
    {
        DestinasjonsOppgave oppgave = new DestinasjonsOppgave();
        oppgave.execute(GRUNN_URL, "GET");
    }

    public ArrayList<Destinasjon> get(String bruker )
    {
        return null; // NOT YET IMPLEMENTED
    }

    public ArrayList<Destinasjon> get(int id )
    {
        return null; // NOT YET IMPLEMENTED
    }

    private class DestinasjonsOppgave extends AsyncTask<String, Void, Long>
    {

        // HTTP statuscodes:
        final static int URL = 0;
        final static int HTTP_METODE = 1;
        final static int PAKKE = 2;

        // Statuscodes:
        final static long FEIL = 0l;
        final static long OK = 1l;
        final static long JSON_ANALYSE_FEIL = 3l;
        final static long IO_FEIL = 4l;
        final static long FEILFORMATERT_URL_FEIL = 5l;


        @Override
        protected Long doInBackground(String... params)
        {
            HttpURLConnection oppkobling = null;
            long resultat = 0L;

            try
            {
                java.net.URL url = new URL(params[URL]);
                oppkobling = (HttpURLConnection) url.openConnection();
                oppkobling.setRequestMethod(params[HTTP_METODE]);
                oppkobling.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                if (params[HTTP_METODE].equals("PUT")
                        || params[HTTP_METODE].equals("POST")
                        || params[HTTP_METODE].equals("DELETE"))
                {
                    oppkobling.setDoInput(true);
                    oppkobling.setDoOutput(true);
                    oppkobling.setChunkedStreamingMode(0);
                    oppkobling.setRequestProperty("Transfer-Encoding", "chunked");
                } else
                {
                    oppkobling.setDoInput(true);
                }
                oppkobling.connect();

                if (params[HTTP_METODE].equals("PUT")
                        || params[HTTP_METODE].equals("POST")
                        || params[HTTP_METODE].equals("DELETE"))
                {
                    OutputStreamWriter ut = new OutputStreamWriter(oppkobling.getOutputStream());
                    ut.write(params[PAKKE]);
                    ut.close();
                }

                if (oppkobling.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return FEIL;

                if (params[HTTP_METODE].equals("GET"))
                {
                    BufferedReader innleser = new BufferedReader(new InputStreamReader(
                            oppkobling.getInputStream())
                    );

                    StringBuilder pakke = new StringBuilder();
                    String linje;

                    while ((linje = innleser.readLine()) != null)
                        pakke.append(linje);
                    analyserData(pakke.toString());
                }

                resultat = OK;
            } catch (MalformedURLException e)
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
            JSONArray tabell = new JSONArray(json);
            ArrayList<Destinasjon> destinasjoner = new ArrayList<>();

            for (int i = 0; i < tabell.length(); i++)
            {
                destinasjoner.add(new Destinasjon( (JSONObject) tabell.get(i)));
            }
            aktivitet.setDestinasjoner(destinasjoner);
            aktivitet.sorterDestinasjoner();
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
            Log.d("ASYNC_POST_EXECUTE", "Result-code=" + resultat + ", Message=" + status);
        }
    }
}
