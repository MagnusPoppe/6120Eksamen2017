package no.byteme.magnuspoppe.eksamen;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.location.Location;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;

import no.byteme.magnuspoppe.eksamen.datamodel.Destination;

public class ActivityMain extends Activity
{

    private static final LatLng HØYSKOLEN = new LatLng(59.408852, 9.059512);

    public static final String MAIN_LAT = "kldaføjsefølakjdf";
    public static final String MAIN_LNG = "asløkdsalskdjfkal";

    private LatLng devicePosition;
    FragmentMap map;
    FragmentCloseLocationList closeLocationList;

    LinearLayout bottomPanel;
    LinearLayout mapPanel;

    private ArrayList<Destination> destinations;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AsyncDestination async = new AsyncDestination(this);
        async.get();

        destinations = new ArrayList<>();

        devicePosition = HØYSKOLEN;
        displayMap();
        displayLocationList();

        bottomPanel = (LinearLayout) findViewById(R.id.locationsListFragmentContainer);
        mapPanel = (LinearLayout) findViewById(R.id.mapFragmentContainer);
        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        transition.setDuration(400);
        bottomPanel.setLayoutTransition(transition);
        mapPanel.setLayoutTransition(transition);
    }

    /**
     * Flytter kameraet til en posisjon.
     * @param position å flytte til.
     */
    public void panMapTo(LatLng position)
    {
        map.panTo(position);
    }

    /**
     * Flytter kameraet til en posisjon og setter en markør i posisjonen.
     * @param title tekst på markøren
     * @param position posisjonen alt skal skje på.
     */
    public void panAndMarkMap(String title, LatLng position)
    {
        map.panTo(position);
        map.markMap(title, position);
    }

    /**
     * Endrer vektingen av de to panelene vist. Disse skal være fokusert på der det er mest
     * viktig informasjon.
     * @param weight Vektingen som skal settes på bunn-panelet.
     */
    public void resizeBottomPanel(float weight)
    {


        LinearLayout.LayoutParams panelParams = (LinearLayout.LayoutParams) bottomPanel.getLayoutParams();
        panelParams.weight = weight;
        bottomPanel.setLayoutParams(panelParams);
        //Animation ani = new AnimateWeightChange(bottomPanel, panelParams.weight, weight);
        //ani.setDuration(1000);




        LinearLayout.LayoutParams mapParams = (LinearLayout.LayoutParams) mapPanel.getLayoutParams();
        mapParams.weight = 1-weight;
        mapPanel.setLayoutParams(mapParams);
        //Animation ani2 = new AnimateWeightChange(bottomPanel, mapParams.weight, 1-weight);
        //ani2.setDuration(1000);

        //bottomPanel.startAnimation(ani);
        // mapPanel.startAnimation(ani2);
    }

    /**
     * Initialiserer lokasjonsliste fragmentet.
     */
    private void displayLocationList()
    {
        // Plasserer ut listen:
        closeLocationList = new FragmentCloseLocationList();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.locationsListFragmentContainer, closeLocationList);
        //transaction.addToBackStack(null);

        // Utfører transaksjonen.
        transaction.commit();
    }

    /**
     * Initialiserer detaljertInfo fragmentet.
     */
    public void displayDetailedInformation(int destinationIndex)
    {
        // Plasserer ut listen:
        FragmentDetailedInfo detailedInfo = new FragmentDetailedInfo();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.locationsListFragmentContainer, detailedInfo);
        transaction.addToBackStack(null);

        // Legger ved hvilken destinasjon som ble valgt.
        Bundle args = new Bundle();
        args.putInt("SELECTED_DESTINATION", destinationIndex);
        detailedInfo.setArguments(args);

        // Utfører transaksjonen.
        transaction.commit();
    }

    /**
     * Viser kartet uten parametere. Dette vil gjøre at
     * kartfragmentet (FragmentMap) vil gjøre sin standard
     * visning av enhetens koordinater.
     */
    private void displayMap()
    {
        displayMap(null);
    }

    /**
     * Viser kartet som fragment inn i "mapFragmentContainer"
     * @param coordinates koordinater kartet skal sentrere seg rundt.
     */
    private void displayMap(LatLng coordinates)
    {
        // Henter nødvendige data
        map = new FragmentMap();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mapFragmentContainer, map);

        // Legger ved koordinater som skal vises i kartet:
        if (coordinates != null)
        {
            Bundle item = new Bundle();
            item.putDouble(MAIN_LAT, coordinates.latitude);
            item.putDouble(MAIN_LNG, coordinates.longitude);
            map.setArguments(item);
            transaction.addToBackStack(null);
        }

        // Utfører transaksjonen.
        transaction.commit();
    }

    /**
     * @return Enhetens lokasjon
     */
    public LatLng getDevicePosition()
    {
        return devicePosition;
    }

    /**
     * @return destinasjonsobjektet
     */
    public ArrayList<Destination> getDestinations()
    {
        return destinations;
    }

    /**
     * Setter destinasjonsobjektet og sorterer det etter enhetens posisjon.
     * @param destinations
     */
    public void setDestinations(ArrayList<Destination> destinations)
    {
        this.destinations = destinations;
        sortAllDestinations();

        if (closeLocationList != null)
            closeLocationList.updateView();
    }

    /**
     * Sorterer alle destinasjoner etter avstand fra bruker. Nærmeste først. (devicePosition)
     */
    public void sortAllDestinations()
    {
        // Lager ny tabell for å sortere i.
        Destination[] sortable = new Destination[destinations.size()];
        int i = 0;

        // Løper igjennom alle objekter og oppdaterer avstand fra enheten:
        for( Destination destination : destinations )
        {
            float[] results = new float[10];
            Location.distanceBetween(
                    devicePosition.latitude, devicePosition.longitude,
                    destination.getCoordinates().latitude, destination.getCoordinates().longitude,
                    results
            );

            destination.setDistanceFromDevice(results[0]);
            sortable[i++] = destination; // Legger til elemeneter inn i en sorterbar tabell
        }

        // Sorterer tabellen:
        Arrays.sort(sortable);

        // Oppdaterer arraylist for visning:
        destinations.clear();
        for( Destination destination : sortable )
            destinations.add(destination);
    }

}
