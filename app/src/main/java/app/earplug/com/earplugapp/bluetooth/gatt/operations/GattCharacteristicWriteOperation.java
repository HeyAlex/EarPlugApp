package app.earplug.com.earplugapp.bluetooth.gatt.operations;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

public class GattCharacteristicWriteOperation extends GattOperation {

    private final UUID mService;
    private final UUID mCharacteristic;
    private final byte[] mValue;

    public GattCharacteristicWriteOperation(UUID service,
                                            UUID characteristic, byte[] value) {
        mService = service;
        mCharacteristic = characteristic;
        mValue = value;
    }

    @Override
    public void execute(BluetoothGatt gatt) {

        BluetoothGattService service = gatt.getService(mService);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(mCharacteristic);
            characteristic.setValue(mValue);
            gatt.writeCharacteristic(characteristic);
        }
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }
}