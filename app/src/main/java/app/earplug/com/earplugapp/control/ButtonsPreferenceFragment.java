package app.earplug.com.earplugapp.control;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.ArrayList;
import java.util.List;

import app.earplug.com.earplugapp.R;

/**
 * @author pavelsalomatov
 *         23.04.17.
 */

public class ButtonsPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    ListPreference selectedAppPreference;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.buttonspreference);

        ListPreference long_tap_functionality = (ListPreference) findPreference("key_long_tap_functionality");
        long_tap_functionality.setOnPreferenceChangeListener(this);

        selectedAppPreference = (ListPreference) findPreference("key_long_tap_selected_app");
        selectedAppPreference.setEnabled("1".equals(long_tap_functionality.getValue()));

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager packageManager = getActivity().getPackageManager();

        List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0);

        List<String> entries = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (ResolveInfo resolveInfo : pkgAppsList) {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = null;
            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            if (appName != null) {
                entries.add(appName);
                values.add(packageName);
            }

        }
        selectedAppPreference.setEntries(entries.toArray(new String[entries.size()]));
        selectedAppPreference.setEntryValues(values.toArray(new String[values.size()]));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        selectedAppPreference.setEnabled("1".equals(o));
        return true;
    }
}
