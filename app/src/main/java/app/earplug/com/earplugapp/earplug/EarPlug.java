package app.earplug.com.earplugapp.earplug;

import android.content.Context;

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

    public void changeVibrationMode() {
        byte[] data;

        byte b = EarPlugOperations.MID_ALERT;
        data = new byte[]{b};

        GattCharacteristicWriteOperation changeLedState = new GattCharacteristicWriteOperation(
                EarPlugConstants.IMMEDIATE_ALERT_UUID,
                EarPlugConstants.IMMEDIATE_ALERT_LEVEL_UUID,
                data);
        mGattMager.queue(changeLedState);
    }

}
