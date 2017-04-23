package app.earplug.com.earplugapp.bluetooth.gatt.operations;

import android.bluetooth.BluetoothGatt;

import app.earplug.com.earplugapp.bluetooth.gatt.GattOperationBundle;

public abstract class GattOperation {

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 1000;
    private GattOperationBundle mBundle;

    public abstract void execute(BluetoothGatt bluetoothGatt);

    public int getTimoutInMillis() {
        return DEFAULT_TIMEOUT_IN_MILLIS;
    }

    public abstract boolean hasAvailableCompletionCallback();

    public GattOperationBundle getBundle() {
        return mBundle;
    }

    public void setBundle(GattOperationBundle bundle) {
        mBundle = bundle;
    }
}