package no.byteme.magnuspoppe.eksamen;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

    @Override
    public void onResume()
    {
        super.onResume();
        ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
        aktivitet.skalerPanelVekting(.4f);
        aktivitet.getLeggTilKnapp().setVisibility(View.VISIBLE);

        // Sjekker alltid om det er uopplastede elementer som venter på å lastes opp.
        aktivitet.lastOppMidlertidigLagret();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        final ActivityCtrl aktivitet = (ActivityCtrl) getActivity();

        View view = inflater.inflate(R.layout.fragment_close_location_list, container, false);

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
