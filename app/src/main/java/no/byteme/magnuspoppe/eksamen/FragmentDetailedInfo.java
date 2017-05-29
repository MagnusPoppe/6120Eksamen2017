package no.byteme.magnuspoppe.eksamen;


import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

import no.byteme.magnuspoppe.eksamen.datamodel.Destination;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentDetailedInfo extends Fragment
{

    ActivityMain activity;
    ImageView image;

    public FragmentDetailedInfo()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detailed_info, container, false);
        activity = (ActivityMain) getActivity();

        Bundle args = getArguments();
        if (args == null)
            getFragmentManager().popBackStack();

        Destination destination = activity.getDestinations().get(
                args.getInt("SELECTED_DESTINATION")
        );

        // Finner tekstviews:
        TextView textName = (TextView) view.findViewById(R.id.details_name);
        TextView textType = (TextView) view.findViewById(R.id.details_type);
        TextView textDescription = (TextView) view.findViewById(R.id.details_description);
        TextView textMoh = (TextView) view.findViewById(R.id.details_moh);
        TextView textOwner = (TextView) view.findViewById(R.id.details_owner);

        // Setter tekstene i forhold til objektet "destinations"
        textName.setText(destination.getName());
        textType.setText(destination.getType());
        textDescription.setText(destination.getDescription());
        textMoh.setText(destination.getMoh()+"");
        textOwner.setText(destination.getOwner());

        if (destination.getImageURL() != null)
        {
            image = (ImageView) view.findViewById(R.id.details_image);
            DownloadImageTask task = new DownloadImageTask();
            task.execute(destination.getImageURL());
        }

        return view;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Long>
    {
        Bitmap bmp;

        @Override
        protected Long doInBackground(String... params)
        {
            try {
                URL url = new URL(params[0]);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }
            catch (IOException e) {
                bmp = null;
                return 0l;
            }
            return 1l;
        }

        @Override
        protected void onPostExecute(Long result)
        {
            super.onPostExecute(result);

            if( result == 1l ) // OK!
                image.setImageBitmap(bmp);
        }
    }
}
