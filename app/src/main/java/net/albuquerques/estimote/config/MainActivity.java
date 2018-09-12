package net.albuquerques.estimote.config;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.recognition.packets.ConfigurableDevice;
import com.estimote.coresdk.recognition.packets.Eddystone;
import com.estimote.coresdk.service.BeaconManager;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "EST_CONF_MAIN_ACTIVITY";

    private BeaconManager beaconManager;
    private List<ConfigurableDevice> devices;
    private Map<String, Beacon> ibeacons;
    private Map<String, Eddystone> eddystones;
    private RecyclerView beaconListRecyclerView;
    private LinearLayoutManager layoutManager;
    private ConfigurableDeviceAdapter beaconAdapter;
    private BeaconRegion beaconRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconListRecyclerView = findViewById(R.id.beacon_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //beaconListRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        beaconListRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        devices = new ArrayList<>();
        ibeacons = new Hashtable<>();
        eddystones = new Hashtable<>();
        beaconAdapter = new ConfigurableDeviceAdapter(devices, ibeacons, eddystones);
        beaconListRecyclerView.setAdapter(beaconAdapter);

        beaconManager = new BeaconManager(this);
        beaconRegion = new BeaconRegion("all_beacons", null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            // set foreground scan periods.
            beaconManager.setForegroundScanPeriod(1000, 0);
            // connects beacon manager to underlying service
            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    // add listener for ConfigurableDevice objects
                    beaconManager.setConfigurableDevicesListener(new BeaconManager.ConfigurableDevicesListener() {
                        @Override
                        public void onConfigurableDevicesFound(List<ConfigurableDevice> configurableDevices) {
                            devices.clear();
                            devices.addAll(configurableDevices);
                            beaconAdapter.notifyDataSetChanged();
                            for(final ConfigurableDevice configurableDevice : configurableDevices) {
                                Log.d(TAG, configurableDevice.toString());
                            }
                        }
                    });
                    beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
                        @Override
                        public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                            for(Beacon beacon: beacons) {
                                ibeacons.put(beacon.getMacAddress().toString(), beacon);
                            }
                        }
                    });
                    beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
                        @Override
                        public void onEddystonesFound(List<Eddystone> beacons) {
                            for(Eddystone eddystone: beacons) {
                                Log.d(TAG, "Eddystone: "+eddystone);
                                eddystones.put(eddystone.macAddress.toString(), eddystone);
                            }
                        }
                    });
                    beaconManager.setErrorListener(new BeaconManager.ErrorListener() {
                        @Override
                        public void onError(Integer errorId) {
                            Log.d(TAG, "Error: "+errorId);
                        }
                    });
                    startDiscovery();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDiscovery();
    }

    private void startDiscovery() {
        ibeacons.clear();
        eddystones.clear();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startConfigurableDevicesDiscovery();
                beaconManager.startRanging(beaconRegion);
                beaconManager.startEddystoneDiscovery();
            }
        });
    }

    private void stopDiscovery() {
        beaconManager.stopConfigurableDevicesDiscovery();
        beaconManager.stopRanging(beaconRegion);
        beaconManager.stopEddystoneDiscovery();
    }
}
