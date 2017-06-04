package no.byteme.magnuspoppe.eksamen;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;
import no.byteme.magnuspoppe.eksamen.datamodel.DestinasjonDB;

import static android.content.Context.MODE_PRIVATE;


/**
 *  Et fragment for å registrere nye turmål. Dette fragmentet er
 *  bygd opp med inndata felter og mulighet for å fange bilde.
 *  Øvrig informasjon settes på forhånd.
 *
 *  Det er også lagt opp for mellomlagring i lokal database om nødvendig.
 *  Dette systemet lagrer brukerens inndata på det mest minimale for å så
 *  laste det opp til global database når mulig.
 */
public class FragmentAddLocation extends Fragment implements mellomLagerBildeKontrakt
{

    // Data som skal lagres:
    Destinasjon denneDestinasjonen;

    // Konstanter for mellomlagring ved rotasjon:
    private static final String ULAGRET_DESTINASJON = "ulagret..";
    private static final String BILDELAGRING = "Bilde lagring.";
    private static final String LOKAL_STI = "lokalsti";
    private static final String FILNAVN = "filanvøaklsdfj";

    // Inndata objekter:
    TextInputEditText innNavn, innType, innBeskrivelse; // Tekst
    ImageView innBilde;                                 // Bildevisning
    Bitmap bilde;                                       // Bildet

    // ID FOR TA BILDE:
    private static final int TA_BILDE_INTENT_ID = 2532523;
    private static final int BILDE_OK = -1;     // "OK" statuskode for ACTION_IMAGE_CAPTURE

    // Strenger brukt for opplasting av bilder
    String lokalSti, filnavn;
    private final static String FILFORMAT = ".jpg";

    public FragmentAddLocation()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Setter policy for å bruke bildelagring:
        // INFO HENTET FRA:
        // https://stackoverflow.com/questions/38555301/android-taking-picture-with-fileprovider
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    /**
     * Lagrer nødvendig data for å hente opp igjen samme
     * fragmentet om f.eks. rotasjon skjer.
     *
     * Det meste lagres i destinasjon når det blir lagret
     * og dermed er dette enkel måte å hente ut på.
     *
     * Bildet lagres også som en bytetabell for å så kunne hentes opp
     * igjen.
     *
     * Sti for bildeplassering lokalt og filnavn må lagres i det sannsynlige
     * tilfellet at enheten returnerer fra en "ta bilde" intent med annen
     * rotasjon enn det en gikk inn med.
     *
     * @param outState data som lagres
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // Lagrer lokasjonsinformasjon
        if (denneDestinasjonen != null)
            outState.putParcelable(ULAGRET_DESTINASJON, denneDestinasjonen);

        // Lagrer bildet:
        if (bilde != null)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bilde.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            outState.putByteArray(BILDELAGRING, byteArray);
        }

        // Lagrer stier:
        if (lokalSti != null)
        {
            outState.putString(LOKAL_STI, lokalSti);
            outState.putString(FILNAVN, filnavn);
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Henter ut dataene som ble lagret ved rotasjon.
     * @param savedInstanceState bundle med data som skal lagres
     */
    @Override
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        // Hvis data er blitt mellomlagret
        if (savedInstanceState != null)
        {
            // Henter lokal sti
            if (savedInstanceState.containsKey(LOKAL_STI))
                lokalSti = savedInstanceState.getString(LOKAL_STI);

            // Henter filnavn
            if (savedInstanceState.containsKey(FILNAVN))
                filnavn = savedInstanceState.getString(FILNAVN);
        }
        super.onViewStateRestored(savedInstanceState);
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
        View view = inflater.inflate(R.layout.fragment_add_location, container, false);

        // Henter "Textviews" for forhåndsdefinert data:
        TextView innLat  = (TextView) view.findViewById(R.id.legg_til_lat);
        TextView innLng  = (TextView) view.findViewById(R.id.legg_til_lng);
        TextView innMoh  = (TextView) view.findViewById(R.id.legg_til_moh);
        TextView innEier = (TextView) view.findViewById(R.id.legg_til_eier);

        // Henter "TextInputEditText"
        innNavn         = (TextInputEditText) view.findViewById(R.id.inndataNavn);
        innType         = (TextInputEditText) view.findViewById(R.id.inndataType);
        innBeskrivelse  = (TextInputEditText) view.findViewById(R.id.inndataBeskrivelse);

        // Bildeholder:
        innBilde = (ImageView) view.findViewById(R.id.innBilde);
        innBilde.setImageResource(R.mipmap.ic_camera);
        innBilde.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                taBilde();
            }
        });

        // Lagreknapp:
        Button lagreKnapp = (Button) view.findViewById(R.id.inndataLagreKnapp);
        lagreKnapp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                lagreDestinasjon();
            }
        });

        // Kontroll på hvor fragmentet blir startet fra, og innehenting av data:
        if (savedInstanceState != null && savedInstanceState.containsKey(ULAGRET_DESTINASJON))
        {
            // Setter tekstfeltene til det de var før rotasjon:
            denneDestinasjonen = savedInstanceState.getParcelable(ULAGRET_DESTINASJON);
            innNavn.setText(denneDestinasjonen.getNavn());
            innType.setText(denneDestinasjonen.getType());
            innBeskrivelse.setText(denneDestinasjonen.getBeskrivelse());

            // Henter bilde hvis det var tatt:
            if (savedInstanceState.containsKey(BILDELAGRING))
            {
                byte[] byteArray = savedInstanceState.getByteArray(BILDELAGRING);
                bilde = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                innBilde.setImageBitmap(bilde);
            }
        }
        else // Normal oppstart:
        {
            // Tre datafelter om lokasjon skal være sendt med:
            Bundle argumenter = getArguments();
            if (argumenter != null)
            {
                // Destinasjonsdata som ble sendt med:
                denneDestinasjonen = new Destinasjon(
                        argumenter.getDouble("MOH"),
                        argumenter.getDouble("LAT"),
                        argumenter.getDouble("LNG")
                );
            }
            // Hvis ikke disse datafeltene var sendt med tvinger vi bruker
            // tilbake til listefragmentet:
            else getFragmentManager().popBackStack();
        }

        // Henter brukernavn fra shared preferences:
        ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
        SharedPreferences innstillinger =
                aktivitet.getSharedPreferences(ActivityCtrl.INNSTILLINGER, MODE_PRIVATE);
        denneDestinasjonen.setEier(innstillinger.getString("email", ""));

        // Setter tekstfelter med lokasjonsdata og eier:
        innEier.setText(denneDestinasjonen.getEier());
        innLat.setText(denneDestinasjonen.getKoordinat().latitude+"");
        innLng.setText(denneDestinasjonen.getKoordinat().longitude+"");
        innMoh.setText(denneDestinasjonen.getMoh() + " " + getResources().getString(R.string.metersAboveSeaLevel));

        // Fjerner legg til knapp og skalerer panelet til korrekt størrelse
        aktivitet.getLeggTilKnapp().setVisibility(View.GONE);
        aktivitet.skalerPanelVekting(ActivityCtrl.STORT_PANEL);

        return view;
    }

    /**
     * Metode for å starte kamera intent med ACTION_IMAGE_CAPTURE.
     * Metoden gjør klart for å ta bilde ved å lage filene på
     * forhånd. Disse filene skal forsikre om at man finner
     * tilbake til bildet etter det ble tatt. Selve intentet
     * returnerer ingen data siden FileProvider lagrer bildet
     * for kameraintentet.
     *
     * Deler av mønster funnet her:
     * https://stackoverflow.com/questions/38555301/android-taking-picture-with-fileprovider
     * og her:
     * https://developer.android.com/reference/android/support/v4/content/FileProvider.html
     */
    public void taBilde()
    {
        // Lagrer intent for å åpne kamera:
        Intent taBildeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Lager filnavnet:
        filnavn = "_" + System.currentTimeMillis() + FILFORMAT;

        // Lager filen som bildet skal være:
        File imagePath = new File(
                getActivity().getApplicationContext().getExternalFilesDir("external_files"), ""
        );
        File file = new File(imagePath, filnavn);

        // Lagerer lokal sti i tilfellet rotasjon mens kamera er i bruk:
        lokalSti = imagePath.getPath();

        // Lagrer URI for hvor kamera intent skal lagre bildet:
        final Uri outputUri = FileProvider.getUriForFile(
                getActivity(),
                "no.byteme.fileprovider",
                file);
        taBildeIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        // Gir kamera tillatelse til å skrive bildefil:
        getActivity().getApplicationContext().grantUriPermission(
                "com.google.android.GoogleCamera",
                outputUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        // Starter kamera:
        if (taBildeIntent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivityForResult(taBildeIntent, TA_BILDE_INTENT_ID);
    }

    /**
     * Denne metoden kjører som resultat av at en intent er blitt
     * brukt. I dette fragmentet er det kamera appen som blir brukt.
     * Kunne gjerne vært fler inteneter i en app.
     *
     * @param requestCode hvilken app som returnerer
     * @param resultCode resultatet fra intent
     * @param data Data hvis det medfølger.
     *
     * Deler av mønster funnet her:
     * https://stackoverflow.com/questions/38555301/android-taking-picture-with-fileprovider
     * og her:
     * https://developer.android.com/reference/android/support/v4/content/FileProvider.html
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Sjekker om kamera returnerer og om resulatet er OK:
        if (requestCode == TA_BILDE_INTENT_ID && resultCode == BILDE_OK)
        {
            // Åpner egen filhåndteringsklasse:
            ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
            ImageHandler filhandtering = new ImageHandler(
                    aktivitet.getApplicationContext(),
                    getView(),
                    this
            );

            // setter bilde URL og viser bildet til bruker i eget vindu:
            denneDestinasjonen.setBildeURL(lokalSti+"/"+filnavn);
            innBilde.setImageBitmap(filhandtering.lastInnBilde(lokalSti, filnavn));

            // Laster opp bildet:
            filhandtering.lastOppBilde(lokalSti, filnavn);

            // Skjuler "legg til" knapp (blir satt tilbake når listen vises igjen):
            aktivitet.getLeggTilKnapp().setVisibility(View.GONE);
        }
    }

    /**
     * Ferdigstiller "denneDestinasjonen objektet med de resterende, ikke
     * lagrede feltene som kreves for å ha et komplett objekt.
     *
     * Lagrer heller NULL objekter enn tomme strenger.
     */
    private void lagreDestinasjon()
    {
        // Henter inn data:
        if (innNavn.getText().length() > 0)
            denneDestinasjonen.setNavn(""+innNavn.getText());
        if (innType.getText().length() > 0)
            denneDestinasjonen.setType(""+innType.getText());
        if (innBeskrivelse.getText().length() > 0)
            denneDestinasjonen.setBeskrivelse(""+innBeskrivelse.getText());

        final ActivityCtrl aktivitet = (ActivityCtrl) getActivity();

        // Kontrollerer at brukerkonto er satt.
        if (denneDestinasjonen.getEier().equals("") && denneDestinasjonen.getEier() != null)
        {
            Snackbar.make(getView(), "Brukerkonto feil!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETT OPP I FEIL", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {aktivitet.visInnstillinger();
                        }
                    })
                    .show();
            return;
        }

        // Hvis det er internett forbindelse skal appen laste opp lokasjonen:
        if(aktivitet.harInternettForbindelse())
        {
            AsynkronDestinasjon oppgave = new AsynkronDestinasjon(aktivitet);
            oppgave.post(denneDestinasjonen);
            denneDestinasjonen.setiGlobalDatabase(true);
        }
        else // Hvis det ikke er internett mellomlagrer appen lokasjonen:
        {
            Snackbar.make(getView(), "Ingen nettilgang. Turmål blir lastet opp senere.",
                    Snackbar.LENGTH_LONG).show();
            denneDestinasjonen.setiGlobalDatabase(false);
            lagreLokalt(denneDestinasjonen);
        }

        // Legger til destinasjon i listen av destinasjoner og returnerer til listen.
        aktivitet.leggTilDestinasjon(denneDestinasjonen);
        getFragmentManager().popBackStack();
    }

    /**
     * Lagerer destinasjonen i lokal database for senere opplasting til
     * internett.
     * @param destinasjon som skal lastes opp
     */
    private void lagreLokalt(Destinasjon destinasjon)
    {
        // Lagrer i lokal database:
        ActivityCtrl aktivtet = (ActivityCtrl) getActivity();
        DestinasjonDB db = aktivtet.getDB();
        db.insertDestinasjon(destinasjon);
    }

    /**
     * Callback metode kalt på fra ImageHandeler klassen.
     * Denne setter korrekt sti til blidet om etter forsøk
     * på opplasting:
     *
     * NOTAT: Ikke enda implementert "etteropplasting av bilde".
     */
    @Override
    public void vedKomplettOpplastingAvBilde(boolean fullført)
    {
        if (fullført)
        {
            denneDestinasjonen.setBildeURL(ImageHandler.URL + filnavn);
        }
        else
        {
            denneDestinasjonen.setBildeURL(lokalSti + "/" + filnavn);
        }
    }
}
