package com.estimote.configuration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.estimote.coresdk.recognition.packets.ConfigurableDevice;
import com.estimote.mgmtsdk.common.exceptions.DeviceConnectionException;
import com.estimote.mgmtsdk.connection.api.DeviceConnection;
import com.estimote.mgmtsdk.connection.api.DeviceConnectionCallback;
import com.estimote.mgmtsdk.connection.api.DeviceConnectionProvider;
import com.estimote.mgmtsdk.feature.settings.SettingCallback;

public class ConfigurationActivity extends AppCompatActivity {
    public static final String TAG = "EST_CONF_CONFIG_ACT";

    private ConfigurableDevice configurableDevice;
    private DeviceConnectionProvider connectionProvider;
    private DeviceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        Intent intent = getIntent();
        configurableDevice = intent.getParcelableExtra(ConfigureBeaconActivity.EXTRA_SCAN_RESULT_ITEM_DEVICE);

        connectionProvider = new DeviceConnectionProvider(this);
        connectToDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(connection != null && !connection.isConnected()) {
            connection.reconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        connection.close();
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
                        connection.settings.beacon.major().get(new SettingCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer value) {
                                Log.d(TAG,"Major: "+value);
                            }
                            @Override
                            public void onFailure(DeviceConnectionException exception) {

                            }
                        });
                    }
                    @Override
                    public void onDisconnected() { }

                    @Override
                    public void onConnectionFailed(DeviceConnectionException e) {
                       throw new RuntimeException(e);
                    }
                });
                }
            });
        }
    }
}
