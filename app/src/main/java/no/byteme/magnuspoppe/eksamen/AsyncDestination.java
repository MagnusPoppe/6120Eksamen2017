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

import no.byteme.magnuspoppe.eksamen.datamodel.Destination;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class AsyncDestination
{
    private final String LIVE = "http://itfag.usn.no/~210852/api.php/destination/";
    ActivityMain activity;

    public AsyncDestination(ActivityMain activity)
    {
        this.activity = activity;
    }

    public void get()
    {
        destinationTask task = new destinationTask();
        task.execute(LIVE, "GET");
    }

    public ArrayList<Destination> get( String userID )
    {
        return null; // NOT YET IMPLEMENTED
    }

    public ArrayList<Destination> get( int id )
    {
        return null; // NOT YET IMPLEMENTED
    }

    private class destinationTask extends AsyncTask<String, Void, Long>
    {

        // HTTP statuscodes:
        final static int URL = 0;
        final static int HTTP_METHOD = 1;
        final static int PAYLOAD = 2;

        // Statuscodes:
        final static long ERROR = 0l;
        final static long OK = 1l;
        final static long JSON_PARSE_ERROR = 3l;
        final static long IO_ERROR = 4l;
        final static long MALFORMED_URL_EXCEPTION = 5l;


        @Override
        protected Long doInBackground(String... params)
        {
            HttpURLConnection connection = null;
            long result = 0L;

            try
            {
                java.net.URL url = new URL(params[URL]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(params[HTTP_METHOD]);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                if (params[HTTP_METHOD].equals("PUT")
                        || params[HTTP_METHOD].equals("POST")
                        || params[HTTP_METHOD].equals("DELETE"))
                {
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setChunkedStreamingMode(0);
                    connection.setRequestProperty("Transfer-Encoding", "chunked");
                } else
                {
                    connection.setDoInput(true);
                }
                connection.connect();

                if (params[HTTP_METHOD].equals("PUT")
                        || params[HTTP_METHOD].equals("POST")
                        || params[HTTP_METHOD].equals("DELETE"))
                {
                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    out.write(params[PAYLOAD]);
                    out.close();
                }

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return ERROR;

                if (params[HTTP_METHOD].equals("GET"))
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream())
                    );

                    StringBuilder payload = new StringBuilder();
                    String responseString;

                    while ((responseString = reader.readLine()) != null)
                        payload.append(responseString);
                    parseData(payload.toString());
                }

                result = OK;
            } catch (MalformedURLException e)
            {
                Log.e("ASYNC ERROR", "SOMETHING WENT WRONG: " + e);
                result = MALFORMED_URL_EXCEPTION;
            } catch (IOException e)
            {
                Log.e("ASYNC ERROR", "SOMETHING WENT WRONG: " + e);
                result = IO_ERROR;
            } catch (JSONException e)
            {
                Log.e("ASYNC ERROR", "JSON ERROR... : " + e);
                result = JSON_PARSE_ERROR;
            } finally
            {
                if (connection != null)
                    connection.disconnect();
            }
            return result;
        }

        /**
         * Lager objekter ut av de mottatte dataene. Dataene blir også sortert.
         * Jeg har valgt å gjøre dette i bakgrunn grunnet at det er en tung prosess som
         * ikke er ønsket på UI tråden.
         *
         * @param json data gotten from the traffic branch.
         * @throws JSONException
         */
        private void parseData(String json) throws JSONException
        {
            JSONArray array = new JSONArray(json);
            ArrayList<Destination> destinations = new ArrayList<>();

            for (int i = 0; i < array.length(); i++)
            {
                destinations.add(new Destination( (JSONObject) array.get(i)));
            }
            activity.setDestinations(destinations);
            activity.sortAllDestinations();
        }

        @Override
        protected void onPostExecute(Long result)
        {
            super.onPostExecute(result);

            String statusmessage = "";

            if (result == OK)
            {
                statusmessage = "Executed rest call successfully.";
            } else if (result == MALFORMED_URL_EXCEPTION)
            {
                statusmessage = "Bad url...";
            } else if (result == IO_ERROR)
            {
                statusmessage = "Problems reading the downloaded data. ( I/O ERROR )";
            } else if (result == JSON_PARSE_ERROR)
            {
                statusmessage = "Bad JSON format on data.";
            } else if (result == ERROR)
            {
                statusmessage = "Something went wrong.";
            } else
            {
                statusmessage = "Unknown errorcode... WHAT?!";
            }
            Log.d("ASYNC_POST_EXECUTE", "Result-code=" + result + ", Message=" + statusmessage);
        }
    }
}
