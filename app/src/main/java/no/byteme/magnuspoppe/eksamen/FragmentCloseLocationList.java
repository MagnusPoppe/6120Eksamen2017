package no.byteme.magnuspoppe.eksamen;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCloseLocationList extends Fragment
{
    ArrayList<Destinasjon> destinasjoner;
    AdapterDestinasjoner adapter;
    ListView destinasjonslisten;
    ActivityCtrl aktivitet;
    LinearLayout feilPanel;

    public static final String SELECTED_DESTINATION = "aksjdhfalskjdfh";

    public FragmentCloseLocationList()
    {
        // Required empty public constructor
    }

    /**
     * Oppdaterer listen. Dette brukes ved endring i datasettet.
     */
    public void oppdaterListen()
    {
        if (destinasjonslisten != null)
            destinasjonslisten.invalidate();
    }

    /**
     * Feilsøkingsmetode for å oppdage hvorfor listen er tom:
     * Gir to resultater om listen er tom:
     * 1. Enheten er ikke på nett.
     * 2. Enheten er på nett. Dette må bety at databasen er tom.
     * Resultatet er uansett at bruker blir presentert med "oppdater"
     * knapp.
     */
    void sjekkOmTomListe()
    {
        if (destinasjoner.isEmpty())
        {
            if (!aktivitet.harInternettForbindelse())
                visOppdaterPanel(getResources().getString(R.string.list_empty));
            else
                visOppdaterPanel(getResources().getString(R.string.empty_database));
        }
        else
        {
            feilPanel.setVisibility(View.GONE);
            destinasjonslisten.invalidate();
        }
    }

    private void visOppdaterPanel(String feilmelding)
    {
        // Finner elementer som skal vises:
        TextView feilstatusfelt = (TextView) getView().findViewById(R.id.feilstatus);

        // Viser feilPanel:
        feilPanel.setVisibility(View.VISIBLE);

        // setter tekst:
        feilstatusfelt.setText(feilmelding);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        aktivitet.skalerPanelVekting(.4f);
        aktivitet.getLeggTilKnapp().setVisibility(View.VISIBLE);

        // Sjekker alltid om det er uopplastede elementer som venter på å lastes opp.
        aktivitet.lastOppMidlertidigLagret();

        sjekkOmTomListe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        aktivitet = (ActivityCtrl) getActivity();

        View view = inflater.inflate(R.layout.fragment_close_location_list, container, false);
        feilPanel = (LinearLayout) view.findViewById(R.id.feilPanelListe);

        destinasjoner = aktivitet.getDestinasjoner();
        destinasjonslisten = (ListView) view.findViewById(R.id.destinasjonsliste);

        adapter = new AdapterDestinasjoner(
                getActivity().getApplicationContext(),
                destinasjoner
        );

        destinasjonslisten.setAdapter(adapter);
        destinasjonslisten.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Markerer kartet og flytter kamera:
                Destinasjon destinasjon = destinasjoner.get(position);
                aktivitet.getKart().flyttKameraTil(destinasjon.getKoordinat());
                aktivitet.getKart().markerKartet(destinasjon, destinasjon.getKoordinat());
                aktivitet.visDetaljertInformasjonsPanel(position);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }


}
