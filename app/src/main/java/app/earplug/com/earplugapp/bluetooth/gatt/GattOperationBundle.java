package app.earplug.com.earplugapp.bluetooth.gatt;

import java.util.ArrayList;

import app.earplug.com.earplugapp.bluetooth.gatt.operations.GattOperation;

public class GattOperationBundle {
    final ArrayList<GattOperation> mOperations;

    public GattOperationBundle() {
        mOperations = new ArrayList<>();
    }

    public void addOperation(GattOperation operation) {
        mOperations.add(operation);
        operation.setBundle(this);
    }

    public ArrayList<GattOperation> getOperations() {
        return mOperations;
    }
}