package net.albuquerques.estimote.config;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.estimote.coresdk.recognition.packets.ConfigurableDevice;
import com.estimote.mgmtsdk.common.exceptions.DeviceConnectionException;
import com.estimote.mgmtsdk.connection.api.DeviceConnection;
import com.estimote.mgmtsdk.connection.api.DeviceConnectionCallback;
import com.estimote.mgmtsdk.connection.api.DeviceConnectionProvider;

import java.util.UUID;

public class BeaconConfigurationActivity extends AppCompatActivity {
    public static final String TAG = "EST_CONF_CONFIG_ACT";

    private ConfigurableDevice configurableDevice;
    private DeviceConnectionProvider connectionProvider;
    private DeviceConnection connection;
    private Button saveButton;
    private Button syncButton;
    private Button firmwareUpdateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Create");
        setContentView(R.layout.activity_beacon_configuration);
        saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BeaconConfigurationActivity.this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
            }
        });
        syncButton = findViewById(R.id.buttonSync);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connection.isConnected()) {
                    Toast.makeText(BeaconConfigurationActivity.this, R.string.syncing, Toast.LENGTH_SHORT).show();
                    connection.syncSettings(new DeviceConnection.SyncSettingsCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Successfully Synced Settings");
                            Toast.makeText(BeaconConfigurationActivity.this, R.string.sync_successful, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(DeviceConnectionException e) {
                            Log.e(TAG, e.toString());
                            Toast.makeText(BeaconConfigurationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(BeaconConfigurationActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
                }
            }
        });
        firmwareUpdateButton = findViewById(R.id.buttonFirmwareUpdate);
        firmwareUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BeaconConfigurationActivity.this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getIntent();
        configurableDevice = intent.getParcelableExtra(ExampleBeaconConfigurationActivity.EXTRA_SCAN_RESULT_ITEM_DEVICE);
        connectionProvider = new DeviceConnectionProvider(this);
        connectToDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resume");
        if(connection != null && !connection.isConnected()) {
            connection.reconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Stop");
        connection.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy");
        connection.destroy();
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
                        Toast.makeText(BeaconConfigurationActivity.this, R.string.device_connected, Toast.LENGTH_SHORT).show();
                        connection.settings.beacon.proximityUUID().get(new SettingDisplay<UUID>(findViewById(R.id.editUuid)));
                        connection.settings.beacon.major().get(new SettingDisplay<Integer>(findViewById(R.id.editMajor)));
                        connection.settings.beacon.minor().get(new SettingDisplay<Integer>(findViewById(R.id.editMinor)));
                    }
                    
                    @Override
                    public void onDisconnected() {
                        Log.d(TAG, "Device Disconnected: "+connection.getDevice());
                        Toast.makeText(BeaconConfigurationActivity.this, R.string.device_disconnected  , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionFailed(DeviceConnectionException e) {
                        Log.e(TAG, e.toString());
                        Toast.makeText(BeaconConfigurationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                }
            });
        }
    }
}
