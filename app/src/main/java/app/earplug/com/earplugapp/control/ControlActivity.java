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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import app.earplug.com.earplugapp.R;
import app.earplug.com.earplugapp.cam.CamService;
import app.earplug.com.earplugapp.earplug.EarPlugConstants;
import app.earplug.com.earplugapp.earplug.EarPlugOperations;
import app.earplug.com.earplugapp.earplug.EarPlugService;
import app.earplug.com.earplugapp.util.PrefUtils;

public class ControlActivity extends AppCompatActivity implements View.OnClickListener {
    private static String mAddress = "";
    private static String mName = "";
    private Button disc_con_button;
    public static final String ADDRESS = "address";
    public static final String NAME = "name";

    private boolean isConnected;
    private EarPlugService mBluetoothLeService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        disc_con_button = (Button) findViewById(R.id.dis_con_button);
        toolbar.setTitle(R.string.app_name);
        final Intent intent = getIntent();
        if (intent != null) {
            mAddress = intent.getStringExtra(ADDRESS);
            mName = intent.getStringExtra(NAME);
        }

        View light_btn = findViewById(R.id.light_btn);
        light_btn.setOnClickListener(this);

        View buttons_btn = findViewById(R.id.buttons_btn);
        buttons_btn.setOnClickListener(this);

        View vibro_btn = findViewById(R.id.vibro_btn);
        vibro_btn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Intent bindIntent = new Intent(this, EarPlugService.class);
//        startService(bindIntent);
//        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
//        registerServiceReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        unbindService(mServiceConnection);
//        mBluetoothLeService = null;
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
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
            mName = mBluetoothLeService.getEarPlug().getmName();
            mAddress = mBluetoothLeService.getEarPlug().getmAddres();
            mBluetoothLeService = ((EarPlugService.LocalBinder) service).getService();
            if (mBluetoothLeService.getConnectionState() != EarPlugConstants.STATE_CONNECTED) {
                mBluetoothLeService.setBluetoothDevice(mAddress, mName);

            }
            onConnectionChanged(mBluetoothLeService.getConnectionState());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    private void onConnectionChanged(int connectionState) {
        ImageView connection_status_img = (ImageView) findViewById(R.id.connection_status_img);
        TextView connection_status = (TextView) findViewById(R.id.connection_status);
        TextView battery_status = (TextView) findViewById(R.id.battery_status);
        switch (connectionState) {
            case EarPlugConstants.STATE_CONNECTING:
                isConnected = false;
                connection_status_img.setImageResource(R.drawable.ic_connecting);
                disc_con_button.setVisibility(View.GONE);
                connection_status.setText(R.string.connecting);
                battery_status.setVisibility(View.GONE);
                break;
            case EarPlugConstants.STATE_CONNECTED:
                isConnected = true;
                disc_con_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBluetoothLeService.getEarPlug().disconnect();
                    }
                });
                disc_con_button.setVisibility(View.GONE);
                disc_con_button.setText(R.string.disconnect_button);
                connection_status_img.setImageResource(R.drawable.ic_connected);
                connection_status.setText(R.string.connected);
                battery_status.setVisibility(View.VISIBLE);
                break;
            case EarPlugConstants.STATE_DISCONNECTED:
                isConnected = false;
                disc_con_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBluetoothLeService.getEarPlug().reconnectLastEarPlug(getApplicationContext());
                    }
                });
                disc_con_button.setVisibility(View.VISIBLE);
                disc_con_button.setText(R.string.connect_button);
                connection_status_img.setImageResource(R.drawable.ic_disconnected);
                connection_status.setText(R.string.dicsonnected);
                battery_status.setVisibility(View.GONE);
                break;
        }
    }

    private void registerServiceReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EarPlugConstants.ACTION_CONNECTION_STATE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.light_btn:
                if(PrefUtils.getFromButtonPrefsString(this,"key_long_tap_functionality","0").equals("0")) {
                    startService(new Intent(this, CamService.class));
                }else{
                    String packagename = PrefUtils.getFromButtonPrefsString(this,"key_long_tap_selected_app","");
                    if(!packagename.isEmpty()){
                        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packagename);
                        startActivity(LaunchIntent);
                    }
                }
                break;
            case R.id.vibro_btn:
                intent = new Intent(this, VibroActivity.class);
                break;
            case R.id.buttons_btn:
                intent = new Intent(this, ButtonsActivity.class);
                break;
        }
        if (intent != null){
            this.startActivity(intent);
        }

    }
}
