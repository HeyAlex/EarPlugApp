package app.earplug.com.earplugapp.control;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author pavelsalomatov
 *         23.04.17.
 */

public class BaseSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);
        //initToolbar();
    }

    private void initToolbar() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            setActionBar(toolbar);
//            ActionBar actionBar = getActionBar();
//            if (actionBar != null) {
//                actionBar.setDisplayShowTitleEnabled(true);
//            }
//            toolbar.setNavigationIcon(R.drawable.ic_toolbar_cancel_tinted);
//            toolbar.setNavigationContentDescription(R.string.action_cancel);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.activity_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_done:
//                saveAndFinish();
//                return true;
//            case android.R.id.home:
//                finish();
//                return true;
//            default:
        return super.onOptionsItemSelected(item);
//        }
    }

    private void saveAndFinish() {
//        SettingsFragment fragment = (SettingsFragment) getFragmentManager().findFragmentByTag
//                (getString(R.string.settings_fragment_tag));
//        fragment.saveSelectedItems();
//        setResult(RESULT_OK);
//        finish();
    }
}
