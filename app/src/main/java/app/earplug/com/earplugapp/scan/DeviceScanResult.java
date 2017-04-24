package app.earplug.com.earplugapp.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.text.TextUtils;

public class DeviceScanResult {
    private ScanResult mScanResult;
    private BluetoothDevice mDevice;
    private String mName;
    private int mRssi;
    private int mTxPowerLevel;
    private long mTimeStampNanos;

    public DeviceScanResult(ScanResult result) {
        mScanResult = result;
        mDevice = result.getDevice();
        mName = mDevice.getName();
        mRssi = result.getRssi();
        mTimeStampNanos = result.getTimestampNanos();
        mTxPowerLevel = result.getScanRecord().getTxPowerLevel();
    }

    public DeviceScanResult(BluetoothDevice device, String fallbackName, int rssi,
                            int txPoweLevel, long timestampNanos) {
        mScanResult = null;
        mDevice = device;
        mName = mDevice.getName();
        if (TextUtils.isEmpty(mName)) {
            mName = fallbackName;
        }
        mRssi = rssi;
        mTimeStampNanos = timestampNanos;
        mTxPowerLevel = txPoweLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceScanResult that = (DeviceScanResult) o;
        return mDevice.equals(that.mDevice);
    }

    @Override
    public int hashCode() {
        return mDevice.hashCode();
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public int getRssi() {
        return mRssi;
    }

    public int getTxPowerLevel() {
        return mTxPowerLevel;
    }

    public long getTimeStampNanos() {
        return mTimeStampNanos;
    }
}