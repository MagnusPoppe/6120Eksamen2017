package no.byteme.magnuspoppe.eksamen;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

import static no.byteme.magnuspoppe.eksamen.ActivityCtrl.UTVALGT;


/**
 * Dette fragmentet viser detaljert informasjon om et fragment.
 * Detaljert informasjon vil si alle data vi har om en destinasjon/
 * turmål.
 *
 * Informasjonen er:
 * Navn på turmål
 * Typen turmål
 * Beskrivelse av turmålet
 * M.O.H., Hvor mange meter over havet turmålet befinner seg
 * Bilde av turmålet om det finnes.
 * Hvem som markerete turmålet (epost adresse)
 * Posisjon turmålet er på kartet, med smud animasjon til destinasjon.
 */

public class FragmentDetailedInfo extends Fragment
{
    // For mellomlagring av bildet ved rotasjon
    private static final String BILDELAGRING = "BILDET";

    // Kontrolleren:
    ActivityCtrl aktivitet;

    // Bildet:
    ImageView detaljbilde;
    Bitmap bilde;

    // Hvilket element turmålet er i arraylisten (datasettet):
    private int utvalgt;

    public FragmentDetailedInfo()
    {
        // Required empty public constructor
    }

    /**
     * Lagerer data før eventuell rotasjon eller annet.
     * Lagerer indeks til Destinasjonselement i datasettet til
     * kontrolleren.
     * Lagerer også bildet som en bytestrøm for raskere innlasting.
     * @param outState bundle som blir lagret.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Indeks:
        outState.putInt(UTVALGT, utvalgt);

        // Lagrer bildet for raskere innhenting:
        if (bilde != null)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bilde.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            outState.putByteArray(BILDELAGRING, byteArray);
        }
    }

    /**
     * Lager selve viewet:
     * @param inflater  Blåser opp layout
     * @param container Holder på view:
     * @param savedInstanceState lagrede data
     * @return ferdig konfigurert fragment view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detailed_info, container, false);
        aktivitet = (ActivityCtrl) getActivity();

        // Finner tekstviews:
        TextView txtNavn = (TextView) view.findViewById(R.id.details_name);
        TextView txtType = (TextView) view.findViewById(R.id.details_type);
        TextView txtBeskrivelse = (TextView) view.findViewById(R.id.detalj_beskrivelse);
        TextView txtMoh = (TextView) view.findViewById(R.id.details_moh);
        TextView txtEier = (TextView) view.findViewById(R.id.detalj_eier);

        Destinasjon destinasjon; // Denne blir satt i kontrollen under:

        // Kontroll for å hente ut data som skal presenteres:
        if (savedInstanceState != null && savedInstanceState.containsKey(UTVALGT))
        {
            utvalgt = savedInstanceState.getInt(UTVALGT);
            destinasjon = aktivitet.getDestinasjoner().get(utvalgt);
            if (destinasjon.getBildeURL() != null)
            {
                // Henter bilde fra pakke istendenfor å gjøre enda en async oppgave:
                detaljbilde = (ImageView) view.findViewById(R.id.detalj_bilde);

                if (savedInstanceState.containsKey(BILDELAGRING))
                {
                    byte[] byteArray = savedInstanceState.getByteArray(BILDELAGRING);
                    bilde = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    detaljbilde.setImageBitmap(bilde);
                }
                else // Hvis bildet ikke var lagret, men finnes alikevel:
                {
                    DownloadImageTask task = new DownloadImageTask();
                    task.execute(destinasjon.getBildeURL());
                }
            }
        }
        else // Første åpning av detaljvisning:
        {
            Bundle args = getArguments();
            if (args == null || !args.containsKey(UTVALGT))
            {
                // Hvis det ikke er lagt ved en utvalgt destinasjon går appen
                // tilbake til start. EDGECASE!
                getFragmentManager().popBackStack();
            }
            utvalgt = args.getInt(UTVALGT);

            destinasjon = aktivitet.getDestinasjoner().get(utvalgt);
            // Sjekker om det tilhører et bilde:
            if (destinasjon.getBildeURL() != null)
            {
                detaljbilde = (ImageView) view.findViewById(R.id.detalj_bilde);
                DownloadImageTask task = new DownloadImageTask();
                task.execute(destinasjon.getBildeURL());
            }
        }

        // Setter tekstene i forhold til objektet "destinasjoner"
        txtNavn.setText(destinasjon.getNavn());
        txtType.setText(destinasjon.getType());
        txtBeskrivelse.setText(destinasjon.getBeskrivelse());
        txtMoh.setText(destinasjon.getMoh()+"");
        txtEier.setText(destinasjon.getEier());

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
        // Setter grafiske elementer ettersom at vinduet vises:
        aktivitet.setDetaljinfoVises(true);
        aktivitet.skalerPanelVekting(ActivityCtrl.STORT_PANEL);
        aktivitet.getLeggTilKnapp().setVisibility(View.GONE);
    }

    /**
     * Jeg bruker onPause() til å styre at aktiviteten vet at dette
     * vinduet IKKE vises med en enkel boolean.
     */
    @Override
    public void onPause()
    {
        super.onPause();
        ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
        aktivitet.setDetaljinfoVises(false);
        // TODO: Bestem om aktivitet.gåTilLokasjon(); skal skje
    }

    /**
     * En egen asynkron oppgave for å laste ned et enkelt bilde med oppgitt url.
     * Følger samme mønster som andre Asynkrone oppgaver, men med en litt
     * forenklet versjon:
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Long>
    {
        final static long FEIL = 0l;
        final static long OK = 1l;

        Bitmap bmp;

        /**
         * Denne oppgaven kjører på egen tråd. Her lastes bildet ned.
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
            // hvis feil intreffer:
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
            {
                bilde = bmp;
                detaljbilde.setImageBitmap(bilde); // Vi er på UI tråd og kan sette på bildet.
            }
            else // NOE GIKK FEIL.
            {
                // Si ifra til brukeren at en feil skjedde.
                if (getView() != null)
                    Snackbar.make(
                            getView(),
                            getResources().getString(R.string.imageError),
                            Snackbar.LENGTH_SHORT
                    ).show();
                Log.e("Async image error", getResources().getString(R.string.imageError));
            }
        }
    }
}
