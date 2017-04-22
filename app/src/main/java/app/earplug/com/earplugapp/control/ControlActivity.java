package app.earplug.com.earplugapp.control;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import app.earplug.com.earplugapp.R;
import app.earplug.com.earplugapp.earplug.EarPlugConstants;
import app.earplug.com.earplugapp.earplug.EarPlugService;

public class ControlActivity extends AppCompatActivity {
    private static String mAddress = "";
    private static String mName = "";
    public static final String ADDRESS = "address";
    public static final String NAME = "name";

    private EarPlugService mBluetoothLeService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);
        final Intent intent = getIntent();
        if (intent != null) {
            mAddress = intent.getStringExtra(ADDRESS);
            mName = intent.getStringExtra(NAME);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent bindIntent = new Intent(this, EarPlugService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        registerServiceReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
    }

    // Handles various events fired by the CometaService.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (EarPlugConstants.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {


                onConnectionChanged(intent.getIntExtra(EarPlugConstants.EXTRA_CONNECTION_STATE,
                        EarPlugConstants.STATE_DISCONNECTED));

            }
        }
    };

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((EarPlugService.LocalBinder) service).getService();
            if (mBluetoothLeService.getConnectionState() != EarPlugConstants.STATE_CONNECTED) {
               mBluetoothLeService.setBluetoothDevice(mAddress, mName);
           }

           // mNameText.setText(mBluetoothLeService.getCometa().getName());
           // mAddresText.setText(mBluetoothLeService.getCometa().getAddres());
            onConnectionChanged(mBluetoothLeService.getConnectionState());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    private void onConnectionChanged(int connectionState) {
        switch (connectionState) {

        }
    }

    private void registerServiceReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EarPlugConstants.ACTION_CONNECTION_STATE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver, intentFilter);
    }

}
