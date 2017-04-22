package app.earplug.com.earplugapp.bluetooth.gatt.operations;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.NonNull;

import com.yotadevices.util.LogCat;

import java.util.UUID;

import app.earplug.com.earplugapp.bluetooth.gatt.GattCharacteristicReadCallback;
import app.earplug.com.earplugapp.util.Preconditions;

public class GattCharacteristicReadOperation extends GattOperation {
    private final UUID mService;
    private final UUID mCharacteristic;
    private final GattCharacteristicReadCallback mCallback;

    public GattCharacteristicReadOperation(@NonNull UUID service,
                                           @NonNull UUID characteristic,
                                           @NonNull GattCharacteristicReadCallback callback) {
        mService = Preconditions.checkNotNull(service);
        mCharacteristic = Preconditions.checkNotNull(characteristic);
        mCallback = Preconditions.checkNotNull(callback);
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        try {
            BluetoothGattCharacteristic characteristic = gatt.getService(mService)
                    .getCharacteristic(mCharacteristic);
            gatt.readCharacteristic(characteristic);
        } catch (NullPointerException ex) {
            LogCat.d("GattCharacterstic null");
        }

    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }

    public void onRead(BluetoothGattCharacteristic characteristic) {
        mCallback.call(characteristic);
    }
}