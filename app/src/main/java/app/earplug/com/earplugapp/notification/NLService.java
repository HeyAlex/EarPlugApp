package app.earplug.com.earplugapp.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import app.earplug.com.earplugapp.earplug.EarPlugService;

/**
 * @author pavelsalomatov
 *         23.04.17.
 */

public class NLService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (EarPlugService.isConnected) {
            EarPlugService.sendSelfIntent(this, EarPlugService.NOTIFICATION_GENERIC_POSTED);
        }
        super.onNotificationPosted(sbn);
    }

}
