package com.estimote.configuration;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.estimote.coresdk.cloud.model.BroadcastingPower;
import com.estimote.coresdk.cloud.model.DeviceFirmware;
import com.estimote.coresdk.recognition.packets.ConfigurableDevice;
import com.estimote.mgmtsdk.common.exceptions.DeviceConnectionException;
import com.estimote.mgmtsdk.connection.api.DeviceConnection;
import com.estimote.mgmtsdk.connection.api.DeviceConnectionCallback;
import com.estimote.mgmtsdk.connection.api.DeviceConnectionProvider;
import com.estimote.mgmtsdk.feature.settings.SettingCallback;
import com.estimote.mgmtsdk.feature.settings.SettingsEditor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class ConfigureBeaconActivity extends AppCompatActivity {
    public static final String TAG = "ESTIMOTE_CONFIG_BEACON";

    private ConfigurableDevice configurableDevice;
    private DeviceConnection connection;
    private DeviceConnectionProvider connectionProvider;

    private LocationManager locationManager;
    private Location currentLocation;

    private TextView beaconIdTextView;
    private Switch autoUpdateSwitch;
    private TextView geolocationValuesTextView;
    private Button saveButton;
    private ProgressDialog progressDialog;

    /*
    Here is a set of predefined UI elements for setting up tag, aisle number and placement.
    Based on tags different majors will be assigned (see tagsMajorsMapping).
    Based on placement apropriate broadcasting powers will be assigned (see placementPowerMapping).
    Based on tags you can also assign different minors by creating similar hash maps and modiffying the writeSettings method.
     */
    private Spinner tagsSpinner;
    private Spinner placementSpinner;

    public static final HashMap<String,Integer> tagsMajorsMapping = new HashMap<String,Integer>() {{
        put("1", 1);
        put("2", 2);
        put("3", 3);
        put("4", 4);
        put("5", 5);
        put("6", 6);
        put("7", 7);
        put("9", 9);
        put("10", 10);
        put("11", 11);
        put("12", 12);
    }};

    public static final HashMap<String,BroadcastingPower> placementPowerMapping = new HashMap<String,BroadcastingPower>() {{
        put("Level 1", BroadcastingPower.LEVEL_1);
        put("Level 2", BroadcastingPower.LEVEL_2);
        put("Level 3", BroadcastingPower.LEVEL_3);
        put("Level 4", BroadcastingPower.LEVEL_4);
        put("Level 5", BroadcastingPower.LEVEL_5);
        put("Level 6", BroadcastingPower.LEVEL_6);
        put("Level 7", BroadcastingPower.LEVEL_7);
        put("Level 8", BroadcastingPower.LEVEL_8);
    }};

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_beacon);

        Intent intent = getIntent();
        configurableDevice = (ConfigurableDevice) intent.getParcelableExtra(MainActivity.EXTRA_SCAN_RESULT_ITEM_DEVICE);

        beaconIdTextView = (TextView) findViewById(R.id.beacon_id_textView);
        beaconIdTextView.setText(configurableDevice.deviceId.toString());
        autoUpdateSwitch = (Switch) findViewById(R.id.auto_update_switch);
        autoUpdateSwitch.setChecked(true);
        geolocationValuesTextView = (TextView) findViewById(R.id.geolocation_values_textView);
        geolocationValuesTextView.setText(R.string.searching);
        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               saveAction();
            }
        });

        tagsSpinner = (Spinner) findViewById(R.id.tags_spinner);
        placementSpinner = (Spinner) findViewById(R.id.placement_spinner);

        ArrayAdapter<String> adapterTags = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                tagsMajorsMapping.keySet().toArray(new String[tagsMajorsMapping.keySet().size()]));

        adapterTags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagsSpinner.setAdapter(adapterTags);

        ArrayAdapter<String> adapterPlacement = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                placementPowerMapping.keySet().toArray(new String[placementPowerMapping.keySet().size()]));
        adapterPlacement.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placementSpinner.setAdapter(adapterPlacement);

        connectionProvider = new DeviceConnectionProvider(this);
        connectToDevice();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectToDevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connection != null && connection.isConnected())
            connection.close();
        locationManager.removeUpdates(locationListener);
    }

    private void connectToDevice() {
        if (connection == null || !connection.isConnected()) {
            connectionProvider.connectToService(new DeviceConnectionProvider.ConnectionProviderCallback() {
                @Override
                public void onConnectedToService() {
                    connection = connectionProvider.getConnection(configurableDevice);
                    connection.connect(new DeviceConnectionCallback() {
                        @Override
                        public void onConnected() {
                            Log.d(TAG, "Connected Device: "+connection.getDevice());
                        }

                        @Override
                        public void onDisconnected() { }

                        @Override
                        public void onConnectionFailed(DeviceConnectionException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    });
                }
            });
        }
    }

    private void saveAction() {
        if (!connection.isConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.wait_until_beacon_connected);
            builder.setCancelable(true);
            builder.setPositiveButton(
                    R.string.alert_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            progressDialog = ProgressDialog.show(this, ".", ".");
            if (autoUpdateSwitch.isChecked()) {
                checkAndUpdateFirmware();
            } else {
                writeSettings();
            }
        }
    }

    private void checkAndUpdateFirmware() {
        progressDialog.setTitle(R.string.checking_firmware);
        progressDialog.setMessage(getString(R.string.fetching_data_from_cloud));

        connection.checkForFirmwareUpdate(new DeviceConnection.CheckFirmwareCallback() {
            @Override
            public void onDeviceUpToDate(DeviceFirmware deviceFirmware) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setTitle(R.string.device_firmware_up_to_date);
                        progressDialog.setMessage(getString(R.string.preparing_for_writing_settings));
                    }
                });
                writeSettings();
            }

            @Override
            public void onDeviceNeedsUpdate(DeviceFirmware deviceFirmware) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setTitle(R.string.updating_firmware);
                        progressDialog.setMessage(getString(R.string.preparing_for_update));
                    }
                });
                connection.updateDevice(new DeviceConnection.FirmwareUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitle(R.string.update_successful);
                                progressDialog.setMessage(getString(R.string.preparing_for_writing_settings));
                            }
                        });
                        writeSettings();
                    }

                    @Override
                    public void onProgress(float v, String s) {
                        final float vF = v;
                        final String sF = s;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setMessage(sF + " " + (vF * 100) + "%");
                            }
                        });
                    }

                    @Override
                    public void onFailure(DeviceConnectionException e) {
                        final DeviceConnectionException eF = e;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                displayError(eF);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(DeviceConnectionException e) {
                final DeviceConnectionException eF = e;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displayError(eF);
                    }
                });
            }
        });
    }

    /*
    Prepare set of settings based on the default or your custom UI.
    Here is also a place to fetch apropriate settings for your device from your custom CMS
    or to save those that were be saved in the onSuccess block before calling displaySuccess.
    */
    private void writeSettings() {
        SettingsEditor edit = connection.edit();
        edit.set(connection.settings.beacon.enable(), true);
        edit.set(connection.settings.deviceInfo.tags(), new HashSet<String>(Arrays.asList((String) tagsSpinner.getSelectedItem())));
        edit.set(connection.settings.beacon.proximityUUID(), UUID.fromString("113069EC-6E64-4BD3-6810-DE01B36E8A3E")); // You might want all your beacons to have a certain UUID.
        edit.set(connection.settings.beacon.major(), tagsMajorsMapping.get((String) tagsSpinner.getSelectedItem()));
        edit.set(connection.settings.beacon.transmitPower(), placementPowerMapping.get((String) placementSpinner.getSelectedItem()).powerInDbm);
        if (currentLocation != null) {
            edit.set(connection.settings.deviceInfo.geoLocation(), currentLocation);
        }

        progressDialog.setTitle(R.string.writing_settings);
        progressDialog.setMessage(getString(R.string.please_wait));
        edit.commit(new SettingCallback() {
            @Override
            public void onSuccess(Object o) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displaySuccess();
                    }
                });
            }

            @Override
            public void onFailure(DeviceConnectionException e) {
                final DeviceConnectionException eF = e;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displayError(eF);
                    }
                });
            }
        });
    }

    private void displaySuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.configuration_succeeded);
        builder.setCancelable(true);
        builder.setPositiveButton(
                R.string.configure_next_beacon,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void displayError(DeviceConnectionException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(e.getLocalizedMessage());
        builder.setCancelable(true);
        builder.setPositiveButton(
                R.string.alert_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            currentLocation = location;
            final Location locationF = location;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    geolocationValuesTextView.setText(String.format("%.2f", locationF.getLatitude()) + ", " + String.format("%.2f", locationF.getLongitude()) + " ±" + locationF.getAccuracy());
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}
