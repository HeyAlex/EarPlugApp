package app.earplug.com.earplugapp.scan;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.yotadevices.util.LogCat;

import java.util.Arrays;

import app.earplug.com.earplugapp.R;
import app.earplug.com.earplugapp.control.ControlActivity;


public class ScanActivity extends AppCompatActivity implements DeviceAdapter.ClickListener {

    private Toolbar mToolbar;
    private RecyclerView mDeviceList;
    private DeviceAdapter mDeviceAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mFirstTime = true;
    private boolean mScanning = true;

    private final static String TAG = ScanActivity.class.getSimpleName();
    private final static int SCANNING_TIME = 4 * 1000;
    private final static int REQUEST_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.search_devices);
        mDeviceList = (RecyclerView) findViewById(R.id.device_list);
        mDeviceList.setHasFixedSize(true);
        mDeviceAdapter = new DeviceAdapter();
        mDeviceList.setAdapter(mDeviceAdapter);
        mDeviceAdapter.setOnItemClickListener(this);
        mLayoutManager = new LinearLayoutManager(this);
        mDeviceList.setLayoutManager(mLayoutManager);
        View refreshButton = findViewById(R.id.refresh_button);
        mHandler = new Handler();
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_BT);
        }

        mBluetoothAdapter = bluetoothManager.getAdapter();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            LogCat.d(TAG, "Unable to obtain a BluetoothLeScanner.");
            return;
        }

        if (mFirstTime) {
            mFirstTime = false;
            onScanningStatusChange();
            scanLeDevice(true);
        }
    }

    public void onScanningStatusChange() {
        View progress = findViewById(R.id.toolbar_progress_bar);
        View refresh = findViewById(R.id.refresh_button);
        View emptyView = findViewById(R.id.empty_view);
        if (mScanning) {
            progress.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            refresh.setVisibility(View.VISIBLE);
        }
    }

    private void scanLeDevice(final boolean enable) {

        LogCat.d(TAG, "startBluetoothLeScan");
        if (enable && mBluetoothAdapter.isEnabled()) {

            mDeviceAdapter.clear();

            // Stop any existing scan first
            mBluetoothLeScanner.stopScan(mLeScanCallback);

            // We're using a null filter here, since it seems the offloaded
            // filtering is broken (on the N5x at least).  We do the filter in
            // the results callback instead.
            //
            // Some details here:
            //    https://code.google.com/p/android/issues/detail?id=180675
            final ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(null)
                    .build();
            final ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothLeScanner.stopScan(mLeScanCallback);
                        onScanningStatusChange();
                    }
                }
            }, SCANNING_TIME);

            mScanning = true;
            onScanningStatusChange();
            mBluetoothLeScanner.startScan(Arrays.asList(scanFilter), scanSettings, mLeScanCallback);
        } else {
            mHandler.removeCallbacksAndMessages(null);
            mScanning = false;
            onScanningStatusChange();
            if (mBluetoothAdapter.isEnabled())
                mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // Sadly, we have to filter for our desired service UUID here, since
            // it seems the offloaded filtering is broken (on the N5x at least)
            // Some details here:
            //    https://code.google.com/p/android/issues/detail?id=180675
            //     final ScanFilter scanFilter = new ScanFilter.Builder()
            //             .setServiceUuid(mServiceUuid)
            //            .build();
            //    if (!scanFilter.matches(result)) return;

            DeviceScanResult resultWrapper = new DeviceScanResult(result);
            mDeviceAdapter.addDevice(resultWrapper);
            mDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            LogCat.d(TAG, "Failed to start scan, error code: " + errorCode);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onItemClick(int position, View v) {
        BluetoothDevice device = mDeviceAdapter.getItem(position).getDevice();

        if ("EarPlug".equals(device.getName())) {
            Intent intent = new Intent(ScanActivity.this, ControlActivity.class);
            intent.putExtra(ControlActivity.ADDRESS, device.getAddress());
            intent.putExtra(ControlActivity.NAME, device.getName());
            startActivity(intent);
        } else {
            Snackbar.make(mToolbar, "This device not supported", Snackbar
                    .LENGTH_LONG).show();
        }

    }

    @Override
    public void onItemLongClick(int position, View v) {
        /*NOP*/
    }
}
