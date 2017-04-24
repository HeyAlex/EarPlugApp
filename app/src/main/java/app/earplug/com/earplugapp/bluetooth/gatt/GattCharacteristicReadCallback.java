package app.earplug.com.earplugapp.bluetooth.gatt;

import android.bluetooth.BluetoothGattCharacteristic;

public interface GattCharacteristicReadCallback {
    void call(BluetoothGattCharacteristic characteristic);
}