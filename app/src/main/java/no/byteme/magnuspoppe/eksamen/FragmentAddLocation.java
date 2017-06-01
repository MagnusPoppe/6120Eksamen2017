package no.byteme.magnuspoppe.eksamen;


import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddLocation extends Fragment
{
    // Data som skal lagres:
    Destinasjon denneDestinasjonen;

    // Inndata objekter:
    TextInputEditText innNavn, innType, innBeskrivelse;

    public FragmentAddLocation()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_location, container, false);

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

        TextView innLat = (TextView) view.findViewById(R.id.add_lat);
        TextView innLng = (TextView) view.findViewById(R.id.add_lng);
        TextView innMoh = (TextView) view.findViewById(R.id.add_moh);

        innLat.setText(denneDestinasjonen.getKoordinat().latitude+"");
        innLng.setText(denneDestinasjonen.getKoordinat().longitude+"");
        innMoh.setText(denneDestinasjonen.getMoh() + " "
                + getResources().getString(R.string.metersAboveSeaLevel));

        // Henter "TextInputEditText"
        innNavn         = (TextInputEditText) view.findViewById(R.id.inndataNavn);
        innType         = (TextInputEditText) view.findViewById(R.id.inndataType);
        innBeskrivelse  = (TextInputEditText) view.findViewById(R.id.inndataBeskrivelse);

        Button lagreKnapp = (Button) view.findViewById(R.id.inndataLagreKnapp);
        lagreKnapp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                lagreDestinasjon();
            }
        });

        return view;
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

        denneDestinasjonen.setEier("magnus@hjemme.no");

        ActivityController aktivitet = (ActivityController) getActivity();

        if(aktivitet.enhetPåNett())
        {
            AsynkronDestinasjon oppgave = new AsynkronDestinasjon(aktivitet);
            oppgave.post(denneDestinasjonen);
        }
        else
        {
            // TODO: Last opp i lokal database.
        }
        aktivitet.leggTilDestinasjon(denneDestinasjonen);
        getFragmentManager().popBackStack();
    }
}
