package no.byteme.magnuspoppe.eksamen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by MagnusPoppe on 12/04/2017.
 */

public class Preferences extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

//        SharedPreferences.OnSharedPreferenceChangeListener listener =
//                new SharedPreferences.OnSharedPreferenceChangeListener()
//                {
//                    @Override
//                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
//                    {
//                        Log.d("PREFERENCES", key + " HAS CHANGED!");
//                    }
//                };
//
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        ActivityController aktivitet = (ActivityController) getActivity();
        aktivitet.visInnstillingPanel(false);
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ActivityController aktivitet = (ActivityController) getActivity();

        SharedPreferences preferences =
                aktivitet.getSharedPreferences(ActivityController.INNSTILLINGER_BRUKER, 0);

        SharedPreferences.Editor editor = preferences.edit();

        String email = sharedPreferences.getString("email", "");

        // Lagrer mulige endringer:
        if (key.equals("email"))
            editor.putString("email", email);

        if (key.equals("firstName"))
            editor.putString("firstName", sharedPreferences.getString("firstName", ""));

        if (key.equals("lastName"))
            editor.putString("lastName", sharedPreferences.getString("lastName", ""));

        editor.commit();

        // Passer på at objektet for brukeren har oppdatert seg:
        aktivitet.brukerOppsett();

        // TODO: Fjern denne:
        Log.v("Settings changed", key + " = " + sharedPreferences.getString(key, ""));
    }
}
