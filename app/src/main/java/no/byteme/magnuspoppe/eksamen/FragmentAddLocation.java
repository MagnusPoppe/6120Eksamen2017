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

import java.io.ByteArrayOutputStream;
import java.io.File;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;
import no.byteme.magnuspoppe.eksamen.datamodel.DestinasjonDB;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddLocation extends Fragment implements mellomLagerBildeKontrakt
{

    // Data som skal lagres:
    Destinasjon denneDestinasjonen;
    final static private String ULAGRET_DESTINASJON = "ulagret..";
    private static final String BILDELAGRING = "Bilde lagring.";
    private static final String LOKAL_STI = "lokalsti";
    private static final String FILNAVN = "filanvøaklsdfj";

    // Inndata objekter:
    TextInputEditText innNavn, innType, innBeskrivelse;
    ImageView innBilde;
    Bitmap bilde;

    // ID FOR TA BILDE:
    private static final int TA_BILDE_INTENT_ID = 2532523;
    private static final int BILDE_OK = -1;

    // strenger brukt for opplasting av bilder
    String lokalSti, filnavn, onlineSti;
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


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (denneDestinasjonen != null)
            outState.putParcelable(ULAGRET_DESTINASJON, denneDestinasjonen);

        if (bilde != null)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bilde.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            outState.putByteArray(BILDELAGRING, byteArray);
        }

        if (lokalSti != null)
        {
            outState.putString(LOKAL_STI, lokalSti);
            outState.putString(FILNAVN, filnavn);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {
            if (savedInstanceState.containsKey(LOKAL_STI))
                lokalSti = savedInstanceState.getString(LOKAL_STI);

            if (savedInstanceState.containsKey(FILNAVN))
                filnavn = savedInstanceState.getString(FILNAVN);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_location, container, false);

        // Henter "Textviews" for forhåndsdefinert data:
        TextView innLat = (TextView) view.findViewById(R.id.legg_til_lat);
        TextView innLng = (TextView) view.findViewById(R.id.legg_til_lng);
        TextView innMoh = (TextView) view.findViewById(R.id.legg_til_moh);

        // Henter "TextInputEditText"
        innNavn         = (TextInputEditText) view.findViewById(R.id.inndataNavn);
        innType         = (TextInputEditText) view.findViewById(R.id.inndataType);
        innBeskrivelse  = (TextInputEditText) view.findViewById(R.id.inndataBeskrivelse);

        // Bildeholder:
        innBilde = (ImageView) view.findViewById(R.id.innBilde);
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
            denneDestinasjonen = savedInstanceState.getParcelable(ULAGRET_DESTINASJON);
            innNavn.setText(denneDestinasjonen.getNavn());
            innType.setText(denneDestinasjonen.getType());
            innBeskrivelse.setText(denneDestinasjonen.getBeskrivelse());

            if (savedInstanceState.containsKey(BILDELAGRING))
            {
                byte[] byteArray = savedInstanceState.getByteArray(BILDELAGRING);
                bilde = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                innBilde.setImageBitmap(bilde);
            }
        }
        else
        {
            Bundle argumenter = getArguments();
            if (argumenter != null)
            {
                denneDestinasjonen = new Destinasjon(
                        argumenter.getDouble("MOH"),
                        argumenter.getDouble("LAT"),
                        argumenter.getDouble("LNG")
                );
            }
            else getFragmentManager().popBackStack();
        }


        SharedPreferences innstillinger =
                getActivity().getSharedPreferences(ActivityCtrl.INNSTILLINGER, MODE_PRIVATE);
        denneDestinasjonen.setEier(innstillinger.getString("email", ""));

        innLat.setText(denneDestinasjonen.getKoordinat().latitude+"");
        innLng.setText(denneDestinasjonen.getKoordinat().longitude+"");
        innMoh.setText(denneDestinasjonen.getMoh() + " " + getResources().getString(R.string.metersAboveSeaLevel));

        return view;
    }

    public void taBilde()
    {
        // Lagrer intent for å åpne kamera:
        Intent taBildeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        filnavn = "_" + System.currentTimeMillis() + FILFORMAT;

        // Lager filen som bildet skal være:
        File imagePath = new File(getActivity().getApplicationContext().getExternalFilesDir("external_files"), "");
        File file = new File(imagePath, filnavn);

        lokalSti = imagePath.getPath();

        final Uri outputUri = FileProvider.getUriForFile(
                getActivity(),
                "no.byteme.fileprovider",
                file);
        taBildeIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        getActivity().getApplicationContext().grantUriPermission(
                "com.google.android.GoogleCamera",
                outputUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        if (taBildeIntent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivityForResult(taBildeIntent, TA_BILDE_INTENT_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TA_BILDE_INTENT_ID && resultCode == BILDE_OK)
        {
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

        // Lagrer bildet lokalt på telefonen:
        // BitmapDrawable bilde = (BitmapDrawable)innBilde.getDrawable();
        // lagreBilde(bilde.getBitmap());

        if(aktivitet.enhetPåNett())
        {
            AsynkronDestinasjon oppgave = new AsynkronDestinasjon(aktivitet);
            oppgave.post(denneDestinasjonen);
            denneDestinasjonen.setiGlobalDatabase(true);
        }
        else
        {
            Snackbar.make(getView(), "Ingen nettilgang. Turmål blir lastet opp senere.",
                    Snackbar.LENGTH_LONG).show();
            denneDestinasjonen.setiGlobalDatabase(false);
            lagreLokalt(denneDestinasjonen);
        }

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
        ActivityCtrl aktivtet = (ActivityCtrl) getActivity();
        DestinasjonDB db = aktivtet.getDB();
        db.insertDestinasjon(destinasjon);
    }

    /**
     * Callback metode kalt på fra ImageHandeler klassen.
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
