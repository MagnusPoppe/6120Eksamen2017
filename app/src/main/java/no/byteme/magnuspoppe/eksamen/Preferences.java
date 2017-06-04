package no.byteme.magnuspoppe.eksamen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Enkel preferences fragment brukt for å endre på brukernavn verdier.
 * Created by MagnusPoppe on 12/04/2017.
 */

public class Preferences extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    /**
     * Standard on-create metode. Denne setter på XML filen for visning.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    /**
     * Registrerer lytter
     */
    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Noen visuelle endringer som må gjøres for å forsikre om at denne siden har en
     * hvit bakgrunn.
     */
    @Override
    public void onPause()
    {
        super.onPause();
        ActivityCtrl aktivitet = (ActivityCtrl) getActivity();
        aktivitet.visInnstillingPanel(false);
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        // Lager nødvendige objekter for å utføre lagringen:
        ActivityCtrl ctrl = (ActivityCtrl) getActivity();
        SharedPreferences preferences = ctrl.getSharedPreferences(ActivityCtrl.INNSTILLINGER, 0);
        SharedPreferences.Editor editor = preferences.edit();

        // Lagrer mulige endringer:
        if (key.equals("email"))
            editor.putString("email", sharedPreferences.getString("email", ""));
        if (key.equals("firstName"))
            editor.putString("firstName", sharedPreferences.getString("firstName", ""));
        if (key.equals("lastName"))
            editor.putString("lastName", sharedPreferences.getString("lastName", ""));

        // Utfører lagringen i UI tråd. Det fungerte ikke å bruke apply() av
        // ukjente grunner. 
        editor.commit();

        // Passer på at objektet for brukeren har oppdatert seg:
        ctrl.brukerOppsett();
    }
}
