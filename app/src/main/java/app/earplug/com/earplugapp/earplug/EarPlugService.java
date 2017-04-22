package app.earplug.com.earplugapp.earplug;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;

public class EarPlugService {

    private final static String TAG = EarPlugService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private String mBluetoothDeviceAddress = "";
    private String mBluetoothDeviceName = "";
    private int mConnectionState = EarPlugConstants.STATE_DISCONNECTED;






}
