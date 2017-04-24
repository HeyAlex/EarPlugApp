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
import app.earplug.com.earplugapp.earplug.EarPlugConstants;
import app.earplug.com.earplugapp.earplug.EarPlugService;

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

        View find_me_btn = findViewById(R.id.find_me_btn);
        find_me_btn.setOnClickListener(this);

        View connection_status_img = findViewById(R.id.connection_status_img);
        connection_status_img.setOnClickListener(this);
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
            try {
                mName = mBluetoothLeService.getEarPlug().getmName();
                mAddress = mBluetoothLeService.getEarPlug().getmAddres();
            } catch (NullPointerException ex) {

            }

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
        View find_me_btn = findViewById(R.id.find_me_btn);
        switch (connectionState) {
            case EarPlugConstants.STATE_CONNECTING:
                isConnected = false;
                connection_status_img.setImageResource(R.drawable.ic_connecting);
                disc_con_button.setVisibility(View.GONE);
                connection_status.setText(R.string.connecting);
                battery_status.setVisibility(View.GONE);
                find_me_btn.setVisibility(View.GONE);
                break;
            case EarPlugConstants.STATE_CONNECTED:
                isConnected = true;
                disc_con_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBluetoothLeService.getEarPlug().disconnect();
                    }
                });
                disc_con_button.setVisibility(View.VISIBLE);
                disc_con_button.setText(R.string.disconnect_button);
                connection_status_img.setImageResource(R.drawable.ic_connected);
                connection_status.setText(R.string.connected);
                battery_status.setVisibility(View.VISIBLE);
                find_me_btn.setVisibility(View.VISIBLE);
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
                find_me_btn.setVisibility(View.GONE);
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
        switch (view.getId()) {
            case R.id.light_btn:
                intent = new Intent(this, LightActivity.class);
                break;
            case R.id.vibro_btn:
                intent = new Intent(this, VibroActivity.class);
                break;
            case R.id.buttons_btn:
                intent = new Intent(this, ButtonsActivity.class);
                break;
            case R.id.find_me_btn:
                boolean oldState = EarPlugService.isSearching;
                boolean newState = !oldState;
                if (mBluetoothLeService != null && EarPlugService.isConnected) {
                    mBluetoothLeService.findEarPlug(newState);
                }
                initFindButton();
                break;
            case R.id.connection_status_img:
                mBluetoothLeService.setBluetoothDevice(mAddress, mName);
                break;
        }
        if (intent != null) {
            this.startActivity(intent);
        }

    }

    private void initFindButton() {
        TextView findPlugText = (TextView) findViewById(R.id.find_plug_txt);
        ImageView findPlugImg = (ImageView) findViewById(R.id.find_plug_img);
        View find_me_btn = findViewById(R.id.find_me_btn);
        if (EarPlugService.isSearching) {
            findPlugText.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_notifications_off_white_24dp), null, null, null);
            findPlugText.setText(R.string.stop_find_plug);
            findPlugImg.setImageResource(R.drawable.ic_notifications_off_white_48dp);
            find_me_btn.setBackgroundResource(R.drawable.card_alert_bg);
        } else {
            findPlugText.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_notifications_active_white_24dp), null, null, null);
            findPlugText.setText(R.string.find_plug);
            findPlugImg.setImageResource(R.drawable.ic_notifications_active_white_48dp);
            find_me_btn.setBackgroundResource(R.drawable.card_primary_bg);
        }
    }
}
