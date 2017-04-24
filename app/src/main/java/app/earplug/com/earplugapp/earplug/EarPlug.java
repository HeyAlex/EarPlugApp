package app.earplug.com.earplugapp.earplug;

import android.content.Context;
import android.widget.Toast;

import app.earplug.com.earplugapp.R;
import app.earplug.com.earplugapp.bluetooth.gatt.GattCharacteristicReadCallback;
import app.earplug.com.earplugapp.bluetooth.gatt.GattManager;
import app.earplug.com.earplugapp.bluetooth.gatt.operations.CharacteristicChangeListener;
import app.earplug.com.earplugapp.bluetooth.gatt.operations.GattCharacteristicWriteOperation;
import app.earplug.com.earplugapp.bluetooth.gatt.operations.GattSetNotificationOperation;
import app.earplug.com.earplugapp.util.Preconditions;

public class EarPlug {

    private GattManager mGattMager;
    private boolean isLedOn = false;
    private GattCharacteristicReadCallback mCallback;
    private String mAddres;

    private String mName;
    private boolean mIsBusy;

    public EarPlug(Context context, GattCharacteristicReadCallback callback, String addres,
                   String name) {
        mGattMager = new GattManager(context, addres);
        mCallback = Preconditions.checkNotNull(callback);
        mAddres = Preconditions.checkNotNull(addres);
        mName = Preconditions.checkNotNull(name);
    }


    public void setNotificationEnabledForButton(CharacteristicChangeListener buttonListener) {
        GattSetNotificationOperation notificationOperation = new GattSetNotificationOperation(
                EarPlugConstants.IMMEDIATE_NOTIFICATION_UUID,
                EarPlugConstants.BUTTON_ALERT_UUID, EarPlugConstants.BUTTON_ALERT_DESCRIPTOR_UUID);
        mGattMager.addCharacteristicChangeListener(EarPlugConstants.BUTTON_ALERT_UUID, buttonListener);
        mGattMager.queue(notificationOperation);
    }

    public void changeVibrationMode(byte type) {
        byte[] data;

        byte b = type;
        data = new byte[]{b};

        GattCharacteristicWriteOperation changeLedState = new GattCharacteristicWriteOperation(
                EarPlugConstants.IMMEDIATE_ALERT_UUID,
                EarPlugConstants.IMMEDIATE_ALERT_LEVEL_UUID,
                data);
        mGattMager.queue(changeLedState);
    }

    public String getmAddres() {
        return mAddres;
    }

    public void setmAddres(String mAddres) {
        this.mAddres = mAddres;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void reconnectLastEarPlug(Context context) {
        try {
            mGattMager = new GattManager(context, mAddres);
        } catch (NullPointerException ex) {
            Toast.makeText(context, R.string.error_on_reconnect, Toast.LENGTH_LONG).show();
        }

    }

    public void disconnect() {
        mGattMager.disconnect();
    }

}
