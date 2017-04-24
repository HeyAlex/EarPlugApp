package app.earplug.com.earplugapp.bluetooth.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.yotadevices.util.LogCat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import app.earplug.com.earplugapp.bluetooth.gatt.operations.CharacteristicChangeListener;
import app.earplug.com.earplugapp.bluetooth.gatt.operations.GattCharacteristicReadOperation;
import app.earplug.com.earplugapp.bluetooth.gatt.operations.GattOperation;
import app.earplug.com.earplugapp.util.Preconditions;

import static app.earplug.com.earplugapp.earplug.EarPlugConstants.ACTION_CONNECTION_STATE_CHANGED;
import static app.earplug.com.earplugapp.earplug.EarPlugConstants.EXTRA_CONNECTION_STATE;
import static app.earplug.com.earplugapp.earplug.EarPlugConstants.STATE_CONNECTED;
import static app.earplug.com.earplugapp.earplug.EarPlugConstants.STATE_CONNECTING;
import static app.earplug.com.earplugapp.earplug.EarPlugConstants.STATE_DISCONNECTED;

public class GattManager {

    private int mConnectionState = STATE_DISCONNECTED;
    private String TAG = GattManager.class.getSimpleName();
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ConcurrentLinkedQueue<GattOperation> mQueue;
    private BluetoothGatt mGatt;
    private GattOperation mCurrentOperation;
    private AsyncTask<Void, Void, Void> mCurrentOperationTimeout;
    private String mAddres;
    private HashMap<UUID, ArrayList<CharacteristicChangeListener>> mCharacteristicChangeListeners;
    private BluetoothDevice mDevice;
    private final static int GATT_ERROR = 133;

    public GattManager(Context context, String addres) {
        mContext = Preconditions.checkNotNull(context);
        mQueue = new ConcurrentLinkedQueue<>();
        mCurrentOperation = null;
        mAddres = Preconditions.checkNotNull(addres);
        mCharacteristicChangeListeners = new HashMap<>();
        initialize();
        connect(mAddres);
    }

    public void disconnect() {
        mGatt.disconnect();
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE mDevice.
     *
     * @param address The mDevice address of the destination mDevice.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void connect(final String address) {
        LogCat.d(TAG, "Connect to " + address);
        if (mBluetoothAdapter == null || address == null) {
            LogCat.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
        }

        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        //mDevice.createBond();
        mDevice.connectGatt(mContext, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (status == GATT_ERROR) {
                    LogCat.d(TAG, "Got the status 133 bug, closing gatt");
                    gatt.close();
                    mGatt = null;
                    return;
                }

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    LogCat.d(TAG, "Gatt connected to mDevice " + mDevice.getAddress());
                    mGatt = gatt;
                    gatt.discoverServices();
                    mConnectionState = STATE_CONNECTED;
                    drive();
                    broadcastConnectionUpdate();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    LogCat.d(TAG, "Disconnected from gatt server " + mDevice.getAddress() + "" +
                            ", newState: " + newState);
                    mGatt = null;
                    setCurrentOperation(null);
                    gatt.close();

                    mConnectionState = STATE_DISCONNECTED;
                    broadcastConnectionUpdate();
                }
            }


            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                ((GattCharacteristicReadOperation) mCurrentOperation).onRead(characteristic);
                setCurrentOperation(null);
                drive();
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                LogCat.d(TAG, "services discovered, status: " + status);
            }


            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic,
                                              int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                LogCat.d(TAG, "Characteristic " + characteristic.getUuid() + "written to on mDevice "
                        + mDevice.getAddress());
                setCurrentOperation(null);
                drive();
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                LogCat.d(TAG, "Characteristic " + characteristic.getUuid() + "was changed, device: " + mDevice.getAddress());
                if (mCharacteristicChangeListeners.containsKey(characteristic.getUuid())) {
                    for (CharacteristicChangeListener listener : mCharacteristicChangeListeners.get(characteristic.getUuid())) {
                        listener.onCharacteristicChanged(characteristic);
                    }
                }
            }
        });

        mConnectionState = STATE_CONNECTING;
        broadcastConnectionUpdate();
    }

    public void broadcastConnectionUpdate() {
        final Intent intent = new Intent(ACTION_CONNECTION_STATE_CHANGED);
        intent.putExtra(EXTRA_CONNECTION_STATE, mConnectionState);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager)
                    mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                LogCat.d(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogCat.d(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            LogCat.d(TAG, "BluetoothAdapter is not enabled.");
            return false;
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            return false;
        }

        return true;
    }

    public synchronized void cancelCurrentOperationBundle() {
        LogCat.d(TAG, "Cancelling current operation. Queue size before: " + mQueue.size());
        if (mCurrentOperation != null && mCurrentOperation.getBundle() != null) {
            for (GattOperation op : mCurrentOperation.getBundle().getOperations()) {
                mQueue.remove(op);
            }
        }
        LogCat.d(TAG, "Queue size after: " + mQueue.size());
        mCurrentOperation = null;
        drive();
    }

    public synchronized void queue(GattOperation gattOperation) {
        mQueue.add(gattOperation);
        LogCat.d(TAG, "Queueing Gatt operation, size will now become: " + mQueue.size());
        drive();
    }

    private synchronized void drive() {
        if (mCurrentOperation != null) {
            LogCat.d(TAG, "tried to drive, but currentOperation was not null, " + mCurrentOperation);
            return;
        }
        if (mQueue.size() == 0) {
            LogCat.d(TAG, "Queue empty, drive loop stopped.");
            mCurrentOperation = null;
            return;
        }

        final GattOperation operation = mQueue.poll();
        LogCat.d(TAG, "Driving Gatt queue, size will now become: " + mQueue.size());
        setCurrentOperation(operation);


        if (mCurrentOperationTimeout != null) {
            mCurrentOperationTimeout.cancel(true);
        }
        mCurrentOperationTimeout = new AsyncTask<Void, Void, Void>() {
            @Override
            protected synchronized Void doInBackground(Void... voids) {
                try {
                    LogCat.d(TAG, "Starting to do a background timeout");
                    wait(operation.getTimoutInMillis());
                } catch (InterruptedException e) {
                    LogCat.d(TAG, "was interrupted out of the timeout");
                }
                if (isCancelled()) {
                    LogCat.d(TAG, "The timeout was cancelled, so we do nothing.");
                    return null;
                }
                LogCat.d(TAG, "Timeout ran to completion, time to cancel the entire operation " +
                        "bundle. Abort, abort!");
                cancelCurrentOperationBundle();
                return null;
            }

            @Override
            protected synchronized void onCancelled() {
                super.onCancelled();
                notify();
            }
        }.execute();

        if (mGatt != null) {
            execute(operation);
        }
    }

    private void execute(GattOperation operation) {
        if (operation != mCurrentOperation) {
            return;
        }
        if (mGatt != null) {
            operation.execute(mGatt);
            if (!operation.hasAvailableCompletionCallback()) {
                setCurrentOperation(null);
                drive();
            }
        }
    }

    public synchronized void setCurrentOperation(GattOperation currentOperation) {
        mCurrentOperation = currentOperation;
    }

    public BluetoothGatt getGatt() {
        return mGatt;
    }

    public void queue(GattOperationBundle bundle) {
        for (GattOperation operation : bundle.getOperations()) {
            queue(operation);
        }
    }

    public void addCharacteristicChangeListener(UUID characteristicUuid,
                                                CharacteristicChangeListener characteristicChangeListener) {
        if (!mCharacteristicChangeListeners.containsKey(characteristicUuid)) {
            mCharacteristicChangeListeners.put(characteristicUuid,
                    new ArrayList<CharacteristicChangeListener>());
        }
        mCharacteristicChangeListeners.get(characteristicUuid).add(characteristicChangeListener);
    }
}