package no.byteme.magnuspoppe.eksamen;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import no.byteme.magnuspoppe.eksamen.datamodel.Destination;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCloseLocationList extends Fragment
{
    ArrayList<Destination> destinations;
    AdapterCloseLocations adapterCloseLocations;
    ListView destinationsList;

    public static final String SELECTED_DESTINATION = "aksjdhfalskjdfh";

    public FragmentCloseLocationList()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_close_location_list, container, false);
        final ActivityMain activityMain = (ActivityMain) getActivity();
        destinations = activityMain.getDestinations();
        destinationsList = (ListView) view.findViewById(R.id.closeLocationsList);

        adapterCloseLocations = new AdapterCloseLocations(
                getActivity().getApplicationContext(),
                destinations
        );

        destinationsList.setAdapter(adapterCloseLocations);
        destinationsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Bundle item = new Bundle();
                // item.putInt(SELECTED_DESTINATION, position);

            }
        });


        // Inflate the layout for this fragment
        return view;
    }

}
