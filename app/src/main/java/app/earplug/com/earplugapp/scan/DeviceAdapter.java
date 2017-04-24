package app.earplug.com.earplugapp.scan;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import app.earplug.com.earplugapp.R;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private ArrayList<DeviceScanResult> mLeDevices;
    private static ClickListener sClickListener;

    public DeviceAdapter() {
        mLeDevices = new ArrayList<DeviceScanResult>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_device, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void clear() {
        mLeDevices.clear();
        notifyDataSetChanged();
    }

    public DeviceScanResult getItem(int position) {
        return mLeDevices.get(position);
    }

    public void addDevice(DeviceScanResult result) {
        if (!mLeDevices.contains(result)) {
            mLeDevices.add(result);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DeviceScanResult device = mLeDevices.get(position);


        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            holder.deviceName.setText(deviceName);
        } else {
            holder.deviceName.setText(R.string.unknown_device);
        }

        holder.deviceAddress.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        public TextView deviceName;
        public TextView deviceAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceAddress = (TextView) itemView.findViewById(R.id.tv_device_address);
            deviceName = (TextView) itemView.findViewById(R.id.tv_device_name);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            sClickListener.onItemClick(getAdapterPosition(), view);
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }


    public void setOnItemClickListener(ClickListener clickListener) {
        DeviceAdapter.sClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);

        void onItemLongClick(int position, View v);
    }
}
