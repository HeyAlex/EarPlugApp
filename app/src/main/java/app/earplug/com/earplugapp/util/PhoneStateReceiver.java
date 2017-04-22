package app.earplug.com.earplugapp.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import app.earplug.com.earplugapp.earplug.EarPlugService;


/**
 * @author pavelsalomatov
 *         23.04.17.
 */

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            EarPlugService.sendSelfIntent(context, EarPlugService.INCOMING_CALL_START, null);
        } else {
            EarPlugService.sendSelfIntent(context, EarPlugService.INCOMING_CALL_END, null);
        }
    }
}