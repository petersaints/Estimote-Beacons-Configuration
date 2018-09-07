package com.estimote.configuration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.recognition.packets.ConfigurableDevice;
import com.estimote.coresdk.recognition.packets.Eddystone;
import com.estimote.coresdk.service.BeaconManager;
import com.estimote.mgmtsdk.common.exceptions.DeviceConnectionException;
import com.estimote.mgmtsdk.connection.api.DeviceConnection;
import com.estimote.mgmtsdk.connection.api.DeviceConnectionCallback;
import com.estimote.mgmtsdk.connection.api.DeviceConnectionProvider;
import com.estimote.mgmtsdk.feature.settings.SettingCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "ESTIMOTE_CONFIG_MAIN";
    public static final String EXTRA_SCAN_RESULT_ITEM_DEVICE = "com.estimote.configuration.SCAN_RESULT_ITEM_DEVICE";

    private BeaconManager beaconManager;
    private TextView devicesCountTextView;
    private BeaconRegion beaconRegion;
    private DeviceConnectionProvider connectionProvider;
    private boolean isConfiguring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        devicesCountTextView = findViewById(R.id.devices_count);
        beaconManager = new BeaconManager(this);
        connectionProvider = new DeviceConnectionProvider(this);
        beaconRegion = new BeaconRegion("all_beacons", null, null, null);
        isConfiguring = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            isConfiguring = false;
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
                            devicesCountTextView.setText(getString(R.string.detected_devices) + ": " + String.valueOf(configurableDevices.size()));
                            // handle the configurable device here. You can use it to acquire connection from DeviceConnectionProvider
                            /*
                            if (!configurableDevices.isEmpty()) {
                                ConfigurableDevice device = configurableDevices.get(0);
                                if (!isConfiguring) {
                                    isConfiguring = true;
                                    stopDiscovery();
                                    Intent intent = new Intent(MainActivity.this, ConfigureBeaconActivity.class);
                                    intent.putExtra(EXTRA_SCAN_RESULT_ITEM_DEVICE, device);
                                    startActivity(intent);
                                }
                            }
                            */
                            for(final ConfigurableDevice configurableDevice : configurableDevices) {
                                /*
                                if(!isConfiguring) {
                                    Log.d(TAG, "Connecting to: "+configurableDevice.deviceId);
                                    isConfiguring = true;
                                    connectionProvider.connectToService(new DeviceConnectionProvider.ConnectionProviderCallback() {
                                        @Override
                                        public void onConnectedToService() {
                                            final DeviceConnection connection = connectionProvider.getConnection(configurableDevice);
                                            connection.connect(new DeviceConnectionCallback() {
                                                @Override
                                                public void onConnected() {
                                                    Log.d(TAG, "Connected to : "+connection.getDevice().deviceId);
                                                    connection.readRssi(new SettingCallback<Integer>() {
                                                        @Override
                                                        public void onSuccess(Integer value) {
                                                            Log.d(TAG, connection.getDevice().deviceId+" RSSI: "+value);
                                                            isConfiguring = false;
                                                            if(connection.isConnected()) {
                                                                connection.close();
                                                            }
                                                        }
                                                        @Override
                                                        public void onFailure(DeviceConnectionException e) {
                                                            Log.d(TAG, "Failure: "+e.getMessage());
                                                        }
                                                    });
                                                }
                                                @Override
                                                public void onDisconnected() {
                                                    Log.d(TAG, "Disconnected: "+configurableDevice.deviceId);

                                                }
                                                @Override
                                                public void onConnectionFailed(DeviceConnectionException e) {
                                                    Log.d(TAG, "Connection Failed: "+e.getMessage());
                                                }
                                            });
                                        }
                                    });
                                }
                                */
                                Log.d(TAG, configurableDevice.toString());
                            }
                        }
                    });
                    beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
                        @Override
                        public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                            for(Beacon beacon: beacons) {
                                Log.d(TAG, "iBeacon: "+ beacon);
                            }
                        }
                    });
                    beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
                        @Override
                        public void onEddystonesFound(List<Eddystone> eddystones) {
                            for(Eddystone eddystone: eddystones) {
                                Log.d(TAG, "Eddystone: "+eddystone);
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
