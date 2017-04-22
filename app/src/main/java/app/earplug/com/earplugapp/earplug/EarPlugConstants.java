package app.earplug.com.earplugapp.earplug;

import java.util.UUID;

/**
 * Created by mac on 22.04.17.
 */

public class EarPlugConstants {

    public final static String ACTION_CONNECTION_STATE_CHANGED =
            "cometa.yotaphone.yotadevice.com.ACTION_CONNECTION_STATE_CHANGED";
    public final static String EXTRA_CONNECTION_STATE =
            "cometa.yotaphone.yotadevice.com.EXTRA_CONNECTION_STATE";

    public final static UUID IMMEDIATE_ALERT_UUID =
            UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public final static UUID IMMEDIATE_ALERT_LEVEL_UUID =
            UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public final static UUID IMMEDIATE_NOTIFICATION_UUID =
            UUID.fromString("00001811-0000-1000-8000-00805f9b34fb");
    public final static UUID BUTTON_ALERT_UUID =
            UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb");
    public final static UUID BUTTON_ALERT_DESCRIPTOR_UUID =
            UUID.fromString("002902-0000-1000-8000-00805f9b34fb");

    public final static String BUTTON_ALERT_STR =
            "00002a46-0000-1000-8000-00805f9b34fb";
    public final static String IMMEDIATE_NOTIFICATION_STR =
            "00001802-0000-1000-8000-00805f9b34fb";

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final int ON_CONNECTION_ID = 10;
}
