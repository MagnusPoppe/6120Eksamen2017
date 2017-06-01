package no.byteme.magnuspoppe.eksamen;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentDetailedInfo extends Fragment
{

    ActivityController aktivitet;
    ImageView detaljbilde;

    public FragmentDetailedInfo()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detailed_info, container, false);
        aktivitet = (ActivityController) getActivity();

        Bundle args = getArguments();
        if (args == null)
            getFragmentManager().popBackStack();

        Destinasjon destinasjon = aktivitet.getDestinasjoner().get(
                args.getInt("SELECTED_DESTINATION")
        );

        // Finner tekstviews:
        TextView txtNavn = (TextView) view.findViewById(R.id.details_name);
        TextView txtType = (TextView) view.findViewById(R.id.details_type);
        TextView txtBeskrivelse = (TextView) view.findViewById(R.id.detalj_beskrivelse);
        TextView txtMoh = (TextView) view.findViewById(R.id.details_moh);
        TextView txtEier = (TextView) view.findViewById(R.id.detalj_eier);

        // Setter tekstene i forhold til objektet "destinasjoner"
        txtNavn.setText(destinasjon.getNavn());
        txtType.setText(destinasjon.getType());
        txtBeskrivelse.setText(destinasjon.getBeskrivelse());
        txtMoh.setText(destinasjon.getMoh()+"");
        txtEier.setText(destinasjon.getEier());

        // Sjekker om bildet er lagt ved til
        if (destinasjon.getBildeURL() != null)
        {
            detaljbilde = (ImageView) view.findViewById(R.id.detalj_bilde);
            DownloadImageTask task = new DownloadImageTask();
            task.execute(destinasjon.getBildeURL());
        }

        // TODO: Åpne intent for å vise i google maps + knapp for å aktivere.

        return view;
    }

    /**
     * Jeg bruker onResume() til å styre at aktiviteten vet at dette
     * vinduet vises med en enkel boolean.
     */
    @Override
    public void onResume()
    {
        super.onResume();
        ActivityController aktivitet = (ActivityController) getActivity();
        aktivitet.setDetaljinfoVises(true);
    }

    /**
     * Jeg bruker onPause() til å styre at aktiviteten vet at dette
     * vinduet IKKE vises med en enkel boolean.
     */
    @Override
    public void onPause()
    {
        super.onPause();
        ActivityController aktivitet = (ActivityController) getActivity();
        aktivitet.setDetaljinfoVises(false);
    }

    /**
     * En egen asynkron oppgave for å laste ned et enkelt bilde med oppgitt url.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Long>
    {
        final static long FEIL = 0l;
        final static long OK = 1l;

        Bitmap bmp;

        /**
         * Denne oppgaven kjører faktisk på egen tråd. Her lastes bildet ned.
         * @param parameter Forventer kun en. Dette skal være URL til bildet.
         * @return OK hvis alt gikk fint, FEIL utenom.
         */
        @Override
        protected Long doInBackground(String... parameter)
        {
            try
            {
                // Forsøker å laste inn bildet hentet fra URL:
                URL url = new URL(parameter[0]);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }
            catch (IOException e)
            {
                return FEIL;
            }

            // Innlasting gikk fint.
            return OK;
        }

        @Override
        protected void onPostExecute(Long resultat)
        {
            super.onPostExecute(resultat);

            // Resultatet sjekkes.
            if( resultat == OK ) // ALT FINT!
                detaljbilde.setImageBitmap(bmp); // Vi er på UI tråd og kan sette på bildet.
            else // NOE GIKK FEIL.
                // Si ifra til brukeren at en feil skjedde.
                Log.e("Async image error", getResources().getString(R.string.imageError));
        }
    }
}
