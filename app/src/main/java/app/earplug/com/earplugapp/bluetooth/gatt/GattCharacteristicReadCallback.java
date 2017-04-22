/*
 * Copyright (C) 2017 YotaDevices, LLC - All Rights Reserved.
 *
 * Confidential and Proprietary.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package app.earplug.com.earplugapp.bluetooth.gatt;


import android.bluetooth.BluetoothGattCharacteristic;

public interface GattCharacteristicReadCallback {
    void call(BluetoothGattCharacteristic characteristic);
}