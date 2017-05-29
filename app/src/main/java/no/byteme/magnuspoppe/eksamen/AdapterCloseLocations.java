package no.byteme.magnuspoppe.eksamen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import no.byteme.magnuspoppe.eksamen.datamodel.Destination;

public class AdapterCloseLocations extends BaseAdapter
{
    Context mContext;
    ArrayList<Destination> destinations;
    LayoutInflater mInflater;

    public AdapterCloseLocations(Context c, ArrayList<Destination> destinations)
    {
        mContext = c;
        this.destinations = destinations;
        mInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount()
    {
        return  destinations.size();
    }

    public Object getItem(int position)
    {
        return destinations.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent)
    {
        // Initializing the view element:
        DestinationView list;
        if (view == null) // Only runs for the first object.
        {
            view = mInflater.inflate(R.layout.list_element_close_location, null);
            list = new DestinationView();
            list.viewName = (TextView) view.findViewById(R.id.destination_name);
            list.viewType = (TextView) view.findViewById(R.id.destination_type);
            list.viewMoh = (TextView) view.findViewById(R.id.destination_moh);
            view.setTag(list);
        }
        else
        {
            list = (DestinationView) view.getTag();
        }

        // Filling with data:
        Destination current = destinations.get(position);
        list.viewName.setText(current.getName());
        list.viewType.setText(current.getType());
        list.viewMoh.setText(current.getMoh()+"");
        return view;
    }

    private static class DestinationView
    {
        public TextView viewName;
        public TextView viewType;
        public TextView viewMoh;
    }
}