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
 * En enkel fragment for å presentere listen av nære
 * lokasjoner. Denne listen er sortert på avstand fra
 * enhetens posisjon, der nærmeste posisjon er først i
 * listen. Man kan også klikke på kartet for å åpne lokasjoner
 * på samme måte som denne listen.
 */
public class FragmentCloseLocationList extends Fragment
{
    // Datasett:
    ArrayList<Destinasjon> destinasjoner;

    // Adapter og listview for presentasjon:
    AdapterDestinasjoner adapter;
    ListView destinasjonslisten;

    // controlleren
    ActivityCtrl aktivitet;

    // Eget panel for feilmelding dersom man ikke får lastet inn datasett.
    LinearLayout feilPanel;

    // Konstanter:
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
        // Hvis ikke det er noen destinasjoner skal feilmelding vises:
        if (destinasjoner.isEmpty())
        {
            // Forskjellige case krever forskjellige feilmelding:
            if (!aktivitet.harInternettForbindelse())
                visFeilmeldingsPanel(getResources().getString(R.string.list_empty));
            else
                visFeilmeldingsPanel(getResources().getString(R.string.empty_database));
        }
        else
        {
            // Oppdaterer listen og skjuler feilpanel.
            feilPanel.setVisibility(View.GONE);
            destinasjonslisten.invalidate();
        }
    }

    /**
     * Viser fram feilmeldingspanelet
     * @param feilmelding som skal vises
     */
    private void visFeilmeldingsPanel(String feilmelding)
    {
        // Finner elementer som skal vises:
        TextView feilstatusfelt = (TextView) getView().findViewById(R.id.feilstatus);

        // Viser feilPanel:
        feilPanel.setVisibility(View.VISIBLE);

        // setter tekst:
        feilstatusfelt.setText(feilmelding);
    }

    /**
     * Når en annen app går tilbake "trykker på tilbakeknappen" skal denne
     * metoden utføres for å konfigurere panelet korrekt:
     */
    @Override
    public void onResume()
    {
        super.onResume();

        // Ordner det visuelle:
        aktivitet.skalerPanelVekting(.4f);
        aktivitet.getLeggTilKnapp().setVisibility(View.VISIBLE);

        // Sjekker alltid om det er uopplastede elementer som venter på å lastes opp.
        aktivitet.lastOppMidlertidigLagret();

        // Sjekker om listen er tom for feilhåndtering:
        sjekkOmTomListe();
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
        // Henter kontroller
        aktivitet = (ActivityCtrl) getActivity();

        // Lager views:
        View view = inflater.inflate(R.layout.fragment_close_location_list, container, false);
        feilPanel = (LinearLayout) view.findViewById(R.id.feilPanelListe);

        // Henter datasett:
        destinasjoner = aktivitet.getDestinasjoner();
        destinasjonslisten = (ListView) view.findViewById(R.id.destinasjonsliste);

        // Konfigurerer adapter for presentasjon av hvert listeelement:
        adapter = new AdapterDestinasjoner(
                getActivity().getApplicationContext(),
                destinasjoner
        );

        // Konfigurerer listview:
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
