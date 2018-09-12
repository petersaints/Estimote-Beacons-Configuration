package com.estimote.configuration;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.recognition.packets.ConfigurableDevice;
import com.estimote.coresdk.recognition.packets.Eddystone;

import java.util.List;
import java.util.Map;

public class ConfigurableDeviceAdapter extends RecyclerView.Adapter<ConfigurableDeviceAdapter.ConfigurableDeviceViewHolder>  {
    private static final String TAG = "EST_CONF_BEACON_ADAPTER";
    private static final String EXTRA_SCAN_RESULT_ITEM_DEVICE = "com.estimote.net.albuquerques.beacons.estimote.configuration.SCAN_RESULT_ITEM_DEVICE";
    private final Map<String, Beacon> ibeacons;
    private final Map<String, Eddystone> eddystones;

    private List<ConfigurableDevice> devices;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ConfigurableDeviceViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public ConfigurableDeviceViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.beacon_text);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ConfigurableDeviceAdapter(List<ConfigurableDevice> devices,
                                     Map<String, Beacon> ibeacons,
                                     Map<String, Eddystone> eddystones) {
        this.devices = devices;
        this.ibeacons = ibeacons;
        this.eddystones = eddystones;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ConfigurableDeviceAdapter.ConfigurableDeviceViewHolder
    onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_beacon, parent, false);
        ConfigurableDeviceViewHolder vh = new ConfigurableDeviceViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ConfigurableDeviceViewHolder holder, final int position) {
        final ConfigurableDevice device = devices.get(position);
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String beaconInfo = "MAC Address: "+device.macAddress.toString()+"\n"
                           +"Type: "+device.type.toString();

        if(ibeacons.containsKey(device.macAddress.toString())) {
            Beacon ibeacon = ibeacons.get(device.macAddress.toString());
            beaconInfo += "\n"
                         +"UUID: "+ibeacon.getProximityUUID()+"\n"
                         +"Major: "+ibeacon.getMajor()+"\n"
                         +"Minor: "+ibeacon.getMinor()+"\n"
                         +"Measured Power: "+ibeacon.getMeasuredPower()+"\n"
                         +"RSSI: "+ibeacon.getRssi();
        }
        if(eddystones.containsKey(device.macAddress.toString())) {
            Eddystone eddystone = eddystones.get(device.macAddress.toString());
            if(eddystone.isUid()) {
                beaconInfo += "\n"
                           + "Namespace: " + eddystone.namespace + "\n"
                           + "Instance: " + eddystone.instance + "\n";
            }
            if (eddystone.isUrl()) {
                beaconInfo += "\n"
                            +"URL: "+eddystone.url+"\n";
            }
            if (eddystone.isEid()) {
                beaconInfo += "\n"
                            + "EID: "+eddystone.eid+"\n";
            }
            beaconInfo += "\n"
                        + "RSSI: "+eddystone.rssi;
        }
        holder.textView.setText(beaconInfo);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked On: "+device.toString());
                    Intent intent = new Intent(v.getContext(), ConfigurationActivity.class);
                    intent.putExtra(EXTRA_SCAN_RESULT_ITEM_DEVICE, device);
                    v.getContext().startActivity(intent);
                }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return devices.size();
    }
}
