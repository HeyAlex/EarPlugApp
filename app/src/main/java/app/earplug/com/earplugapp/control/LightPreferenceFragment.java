package app.earplug.com.earplugapp.control;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import app.earplug.com.earplugapp.R;

/**
 * @author pavelsalomatov
 *         23.04.17.
 */

public class LightPreferenceFragment extends PreferenceFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.lightpreference);
    }
}
