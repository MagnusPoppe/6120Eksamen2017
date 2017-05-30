package no.byteme.magnuspoppe.eksamen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

public class AdapterDestinasjoner extends BaseAdapter
{
    Context mContext;
    ArrayList<Destinasjon> destinasjoner;
    LayoutInflater mInflater;

    public AdapterDestinasjoner(Context c, ArrayList<Destinasjon> destinasjoner)
    {
        mContext = c;
        this.destinasjoner = destinasjoner;
        mInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount()
    {
        return  destinasjoner.size();
    }

    public Object getItem(int position)
    {
        return destinasjoner.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent)
    {
        // Lager objeketet som ligger per oppføring i listen
        DestinasjonsView listeElement;

        if (view == null)
        {
            // Lager "layout" elementet for første gang.
            view = mInflater.inflate(R.layout.list_element_close_location, null);
            listeElement = new DestinasjonsView();
            listeElement.viewNavn = (TextView) view.findViewById(R.id.destination_name);
            listeElement.viewType = (TextView) view.findViewById(R.id.destination_type);
            listeElement.viewMoh = (TextView) view.findViewById(R.id.destination_moh);
            view.setTag(listeElement);
        }
        else
        {
            // Listen var laget fra før.
            listeElement = (DestinasjonsView) view.getTag();
        }

        // Filling with data:
        Destinasjon denne = destinasjoner.get(position);
        listeElement.viewNavn.setText(denne.getNavn());
        listeElement.viewType.setText(denne.getType());
        listeElement.viewMoh.setText(denne.getMoh()+"");
        return view;
    }

    private static class DestinasjonsView
    {
        public TextView viewNavn;
        public TextView viewType;
        public TextView viewMoh;
    }
}