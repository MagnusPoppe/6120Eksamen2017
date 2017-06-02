package no.byteme.magnuspoppe.eksamen;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Enkel klasse for å håndtere alt av bildebehandling.
 * Denne klassen kan lagre bilder lokalt og til netttjener gjennom FTP protokollen.
 * Created by MagnusPoppe on 01/06/2017.
 */

public class ImageHandler
{
    private Context context;
    private View view;
    private FragmentAddLocation fragment;

    public ImageHandler(Context context, View view, FragmentAddLocation fragment)
    {
        this.context = context;
        this.view = view;
        this.fragment = fragment;
    }


    /**
     * Metode for å lagre bilder. Jeg så at dine foiler for henting av bilder allerede var
     * veldig utdatert, så jeg valgte å hente ut en metode fra nettet selv.
     * Metoden er tilpasset appen.
     *
     * Metode hentet fra StackOverflow:
     * https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
     * @param bmp bildefil
     * @return Fullstendig sti til bildefilen.
     * @deprecated siden dette kun er for å lagre en thumbnail.
     */
    public String lagreBilde(Bitmap bmp, String filnavn)
    {
        ContextWrapper cw = new ContextWrapper(context);

        // Lagres i /data/data/yourapp/app_data/
        File directory = cw.getDir(ActivityController.FOTO_LAGER, Context.MODE_PRIVATE);

        // Lager katalog:
        File katalog = new File(directory, filnavn + ".jpg");

        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(katalog);
            // Metoden bruker komprimeringsløsningen fra BitMap klassen
            // for å hente ut en OutputStream av bildet.
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        }
        catch (Exception e)
        {
            if (view != null)
                Snackbar.make(view, "Feil ved lagring av bilde.",Snackbar.LENGTH_LONG).show();
            return null;
        }
        finally
        {
            try
            {
                outputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    /**
     * Metode for å hente ut bilder fra lager.
     * Metoden er tilpasset mitt eget bruk, men hentet fra:
     * https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
     * @param sti til bildekatalogen
     * @param filnavn på bildet
     */
    public Bitmap lastInnBilde(String sti, String filnavn)
    {
        return lastInnBilde(sti+"/"+filnavn);
    }

    public Bitmap lastInnBilde(String fullStiOgNavn)
    {
        Bitmap bilde = null;
        try
        {
            File bildefil = new File(fullStiOgNavn);
            bilde = BitmapFactory.decodeStream(new FileInputStream(bildefil));
        }
        catch (FileNotFoundException e)
        {
            if (view != null)
                Snackbar.make(view, "Fant ikke bilde.",Snackbar.LENGTH_LONG).show();
        }
        return bilde;
    }

    private FileInputStream lastInnBildeFil(String fullStiOgNavn)
    {
        FileInputStream io = null;
        try
        {
            File bildefil =new File(fullStiOgNavn);
            io = new FileInputStream(bildefil);
        }
        catch (FileNotFoundException e)
        {
            if (view != null)
                Snackbar.make(view, "Fant ikke bilde.",Snackbar.LENGTH_LONG).show();
        }
        return io;
    }

    final static public String URL = "http://byteme.no/image/6120BilderEksamen/";
    final static public String SCRIPT = "uploadImage.php";


    public void lastOppBilde(String sti, String navn)
    {
        AsynkronLastOppBilde opplaster = new AsynkronLastOppBilde();
        opplaster.setBildeStiOgNavn(sti, navn);
        opplaster.execute(URL+SCRIPT);
    }

    /**
     * Denne asynkrone oppgaven utfører opplasting av bilde til tjener. Den kobler seg opp
     * mot en PHP fil "API/6120Image/uploadImage.php" som håndterer lagringen av filen
     * på tjener etter opplasting.
     *
     * Opplastingen baserer seg på FORM data som sendes gjennom HTTP kall.
     * Dette er kode hentet fra StackOverflow og blitt tilpasset "ASyncTask"
     * klassen for standard Android oppførsel med tråder.
     *
     * Kode:
     * https://stackoverflow.com/questions/23921356/android-upload-image-to-php-server
     *
     * Denne koden er litt ustabil, og krasjer på "conn.getResponseCode()". Jeg har
     * derfor laget et callback for å beholde bildet på lokalt lager om det ikke blir
     * lastet opp. På denne måten kan det forsøkes igjen.
     */
    private class AsynkronLastOppBilde extends AsyncTask<String, Void, Long>
    {
        // PARAMS verdier:
        final static int URL = 0;

        // Statuscodes:
        final static long FEIL = 0l;
        final static long OK = 1l;


        Bitmap bilde = null;
        Long respons;
        String sti, navn;

        HttpURLConnection conn;
        DataOutputStream stream;
        String boundary = "*****";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        int bytesRead, bytesAvailable, bufferSize;
        int maxBufferSize = 1 * 1024 * 1024;
        byte[] buffer;

        public void setBildeStiOgNavn(String sti, String navn)
        {
            this.sti = sti;
            this.navn = navn;
        }

        @Override
        protected Long doInBackground(String... params)
        {
            try {
                // open a URL connection to the Servlet


                FileInputStream fileInputStream = lastInnBildeFil(sti+"/"+navn);
                URL url = new URL(params[URL]);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", navn);

                stream = new DataOutputStream(conn.getOutputStream());

                stream.writeBytes(twoHyphens + boundary + lineEnd);
                stream.writeBytes(
                        "Content-Disposition: " +
                        "form-data; name=\"uploaded_file\";filename="+ navn + "" + lineEnd);
                stream.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    stream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                stream.writeBytes(lineEnd);
                stream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    respons = OK;
                }

                //close the streams
                fileInputStream.close();
                stream.flush();
                stream.close();

            }
            catch (IOException e)
            {
                respons = FEIL;
            }
            return respons;
        }

        @Override
        protected void onPostExecute(Long resultat)
        {
            super.onPostExecute(resultat);

            // Utfører callback for ferdig opplasting av bilde:
            fragment.vedKomplettOpplastingAvBilde(resultat == OK);
        }
    }
}
