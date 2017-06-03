package no.byteme.magnuspoppe.eksamen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import no.byteme.magnuspoppe.eksamen.datamodel.Destinasjon;

/**
 * Adapter for listview funnet i "FragmentCloseLocationList". Denne
 * viser dataene på en spesielt formatert måte etter:
 * "res/layout/list_element_close_location.xml"
 */
public class AdapterDestinasjoner extends BaseAdapter
{
    Context mContext;
    ArrayList<Destinasjon> destinasjoner;
    LayoutInflater mInflater;

    /**
     * Konstruktør:
     * @param c
     * @param destinasjoner
     */
    public AdapterDestinasjoner(Context c, ArrayList<Destinasjon> destinasjoner)
    {
        mContext = c;
        this.destinasjoner = destinasjoner;
        mInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * @return antall objekter i adapteret:
     */
    public int getCount()
    {
        return  destinasjoner.size();
    }

    /**
     * @param position på objektet som skal hentes.
     * @return objektet på den gitte posisjonen i listen
     */
    public Object getItem(int position)
    {
        return destinasjoner.get(position);
    }

    /**
     * @param position på elementet
     * @return id på element i listen
     */
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * Lager det spesielt formaterte presentasjonen (viewet) som
     * vises per element i listen.
     * @param position
     * @param view
     * @param parent
     * @return
     */
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

    /**
     * Indre klasse for å holde på views.
     */
    private static class DestinasjonsView
    {
        public TextView viewNavn;
        public TextView viewType;
        public TextView viewMoh;
    }
}