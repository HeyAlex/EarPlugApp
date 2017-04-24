package app.earplug.com.earplugapp.earplug;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.yotadevices.util.LogCat;

import java.lang.reflect.Method;

import app.earplug.com.earplugapp.R;
import app.earplug.com.earplugapp.bluetooth.gatt.GattCharacteristicReadCallback;
import app.earplug.com.earplugapp.bluetooth.gatt.operations.CharacteristicChangeListener;
import app.earplug.com.earplugapp.cam.CamService;
import app.earplug.com.earplugapp.notification.ConnectionNotificationMaker;
import app.earplug.com.earplugapp.util.Preconditions;
import app.earplug.com.earplugapp.util.PrefUtils;

public class EarPlugService extends Service implements GattCharacteristicReadCallback, CharacteristicChangeListener {

    private final static String TAG = EarPlugService.class.getSimpleName();

    public static final String INCOMING_CALL_START = "incoming_call_start";
    public static final String INCOMING_CALL_END = "incoming_call_end";
    public static final String NOTIFICATION_GENERIC_POSTED = "notification_generic_posted";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private String mBluetoothDeviceAddress = "";
    private String mBluetoothDeviceName = "";
    private CharacteristicChangeListener characteristicChangeListener;
    private int mConnectionState = EarPlugConstants.STATE_DISCONNECTED;
    private CameraConfig mCameraConfig;
    public EarPlug mEarPlug;
    public static boolean isConnected;
    public static boolean isRinging;
    public static boolean isSearching;

    @Override
    public void onCreate() {
        super.onCreate();
        characteristicChangeListener = this;
        registerServiceReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Handles various events fired by the Service.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int connection_state = intent.getIntExtra(EarPlugConstants.EXTRA_CONNECTION_STATE,
                    EarPlugConstants.STATE_DISCONNECTED);
            if (EarPlugConstants.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                if (connection_state == EarPlugConstants.STATE_CONNECTED) {
                    LogCat.d(TAG, "CONNECTED");
                    if (mConnectionState != EarPlugConstants.STATE_CONNECTED) {
                        mEarPlug.changeVibrationMode(EarPlugOperations.MID_ALERT);
                        mConnectionState = EarPlugConstants.STATE_CONNECTED;
                        NotificationManager nMgr = (NotificationManager) getSystemService(Context
                                .NOTIFICATION_SERVICE);
                        nMgr.cancelAll();
                        startForeground(EarPlugConstants.ON_CONNECTION_ID,
                                new ConnectionNotificationMaker(getApplicationContext())
                                        .makeNotificationConnectionChanged(true));
                        isConnected = true;

                        mEarPlug.setNotificationEnabledForButton(characteristicChangeListener);
                    }
                } else if (connection_state == EarPlugConstants.STATE_DISCONNECTED) {
                    if (mConnectionState != EarPlugConstants.STATE_DISCONNECTED) {

                        mConnectionState = EarPlugConstants.STATE_DISCONNECTED;
                        LogCat.d(TAG, "DISCONNECTED");
                        stopForeground(true);
                        NotificationManager nMgr = (NotificationManager) getSystemService(Context
                                .NOTIFICATION_SERVICE);
                        nMgr.cancelAll();
                        isConnected = false;
                        fireNotification(new ConnectionNotificationMaker(getApplicationContext())
                                .makeNotificationConnectionChanged(false));

                    }
                } else {
                    mConnectionState = EarPlugConstants.STATE_CONNECTING;
                }
            }
        }
    };

    @Override
    public void call(BluetoothGattCharacteristic characteristic) {
        switch (characteristic.getUuid().toString()) {
            case EarPlugConstants.IMMEDIATE_NOTIFICATION_STR: {
                //final String[] value = characteristic.getStringValue(0).split(":");
                final String value = characteristic.getStringValue(1);
                Toast.makeText(this, value, Toast.LENGTH_SHORT).show();

                break;
            }
        }
    }

    public void findEarPlug(boolean onStartFinding) {
        isSearching = onStartFinding;
        if (onStartFinding) {
            mEarPlug.changeVibrationMode(EarPlugOperations.HIGH_ALERT);
        } else {
            mEarPlug.changeVibrationMode(EarPlugOperations.NO_ALERT);
        }
    }

    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        final String value = characteristic.getStringValue(1);
        final String[] spl = value.split(":");
        String valueCharacteristic = spl[1].replaceAll("\\D+","");;

        if (spl[0].contains("long")) {
            if (!isRinging) {
                if (PrefUtils.getFromButtonPrefsString(this, "key_long_tap_functionality", "0").equals("0")) {
                    startService(new Intent(this, CamService.class));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEarPlug.changeVibrationMode(EarPlugOperations.MID_ALERT);
                        }
                    }, 1600);
                } else {
                    String packagename = PrefUtils.getFromButtonPrefsString(this, "key_long_tap_selected_app", "");
                    if (!packagename.isEmpty()) {
                        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packagename);
                        startActivity(LaunchIntent);
                    }
                }
            }

        } else if (spl[0].contains("pressed") && "2".equals(valueCharacteristic)) {
            if (!isRinging) {
                if (PrefUtils.getFromButtonPrefs(this, "key_make_call", true)) {
                    callLastcall();
                }
            }
        } else if (spl[0].contains("pressed") && "1".equals(valueCharacteristic)) {
            if (isRinging) {
                if (PrefUtils.getFromButtonPrefs(this, "key_reject_call", true)) {
                    try {
                        rejectCall();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (spl[0].contains("pressed") && Integer.valueOf(valueCharacteristic) >= 5) {
            if (!isRinging) {
                findPhone();
            }
        }
    }

    public void findPhone() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("FIND DEVCIE OPTION")
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000})
                        .setLights(Color.RED, 3000, 3000);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mBuilder.setSound(alarmSound);
        fireNotification(mBuilder.build());
    }

    public void callLastcall() {
        final ContentResolver resolver = this.getContentResolver();
        Cursor c = null;
        String lastCalledNumber;
        try {
            c = resolver.query(
                    CallLog.Calls.CONTENT_URI,
                    new String[] {CallLog.Calls.NUMBER},
                    null,
                    null,
                    CallLog.Calls.DEFAULT_SORT_ORDER + " LIMIT 1");
            if (c == null || !c.moveToFirst()) {
                lastCalledNumber = "";
            }else{
                lastCalledNumber = c.getString(0);
            }
        } finally {
            if (c != null) c.close();
        }
        Uri call = Uri.parse("tel:" + lastCalledNumber);
        Intent surf = new Intent(Intent.ACTION_CALL, call);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(surf);
    }

    private void rejectCall() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            // Get the getITelephony() method
            Class<?> classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method method = classTelephony.getDeclaredMethod("getITelephony");
            // Disable access check
            method.setAccessible(true);
            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = method.invoke(telephonyManager);
            // Get the endCall method from ITelephony
            Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");
            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public class LocalBinder extends Binder {
        public EarPlugService getService() {
            return EarPlugService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogCat.d(TAG, "onStartCommand");
        if (mBluetoothDeviceAddress.isEmpty()) {
            final boolean initialized = initialize();
        }
        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(INCOMING_CALL_START)) {
                    if (!isRinging) {
                        if (PrefUtils.getFromButtonPrefs(this, "key_enable_vibro_dialer", true)) {
                            mEarPlug.changeVibrationMode(EarPlugOperations.HIGH_ALERT);
                        }
                    }
                    isRinging = true;
                } else if (intent.getAction().equals(INCOMING_CALL_END)) {
                    if (isRinging) {
                        isRinging = false;
                        mEarPlug.changeVibrationMode(EarPlugOperations.NO_ALERT);
                    }
                } else if (intent.getAction().equals(NOTIFICATION_GENERIC_POSTED)) {
                    if (!isRinging) {
                        if (PrefUtils.getFromButtonPrefs(this, "key_enable_vibro_notifications", true)) {
                            mEarPlug.changeVibrationMode(EarPlugOperations.MID_ALERT);
                        }
                    }
                }
            }
        }
        return START_STICKY;
    }

    private void registerServiceReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EarPlugConstants.ACTION_CONNECTION_STATE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver, intentFilter);
    }

    public void setBluetoothDevice(String address, String name) {
        mBluetoothDeviceAddress = Preconditions.checkNotNull(address, "addres == null");
        mBluetoothDeviceName = Preconditions.checkNotNull(name, "name == null");
        mEarPlug = new EarPlug(this, this, mBluetoothDeviceAddress, mBluetoothDeviceName);
    }

    private void fireNotification(Notification notification) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogCat.d(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            LogCat.d(TAG, "BluetoothAdapter is not enabled.");
            return false;
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            LogCat.d(TAG, "Unable to obtain a BluetoothLeScanner.");
            return false;
        }
        return true;
    }

    public int getConnectionState() {
        return mConnectionState;
    }

    public EarPlug getEarPlug() {
        return mEarPlug;
    }

    public static void sendSelfIntent(Context context, String action) {
        Intent serviceIntent = new Intent(context, EarPlugService.class);
        serviceIntent.setAction(action);
        serviceIntent.setPackage(context.getPackageName());
        context.startService(serviceIntent);
    }

}
