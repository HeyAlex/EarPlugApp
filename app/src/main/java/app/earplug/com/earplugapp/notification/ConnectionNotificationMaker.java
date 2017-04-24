package app.earplug.com.earplugapp.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import app.earplug.com.earplugapp.R;
import app.earplug.com.earplugapp.control.ControlActivity;
import app.earplug.com.earplugapp.scan.ScanActivity;
import app.earplug.com.earplugapp.util.Preconditions;

public class ConnectionNotificationMaker {

    private static final int SMALL_ICON_ID = R.mipmap.ic_launcher;

    private final Context mContext;

    public ConnectionNotificationMaker(Context context) {
        mContext = Preconditions.checkNotNull(context, "context == null");
    }

    public Notification makeNotificationConnectionChanged(boolean isConnected) {

        if (isConnected) {
            Intent resultIntent = new Intent(mContext, ControlActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            return new Notification.Builder(mContext)
                    .setContentTitle(mContext.getString(R.string.connected_notification))
                    .setSmallIcon(SMALL_ICON_ID)
                    .setOngoing(true)
                    .setContentIntent(resultPendingIntent)
                    .build();
        } else {
            Intent resultIntent = new Intent(mContext, ScanActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            return new Notification.Builder(mContext)
                    .setContentTitle(mContext.getString(R.string.disconnected_notification))
                    .setSmallIcon(SMALL_ICON_ID)
                    .setOngoing(false)
                    .setContentIntent(resultPendingIntent)
                    .build();
        }
    }
}
