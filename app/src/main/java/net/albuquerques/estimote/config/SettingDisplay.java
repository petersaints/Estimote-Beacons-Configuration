package net.albuquerques.estimote.config;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.estimote.mgmtsdk.common.exceptions.DeviceConnectionException;
import com.estimote.mgmtsdk.feature.settings.SettingCallback;

public class SettingDisplay<T> implements SettingCallback<T> {
    public static final String TAG = "EST_CONF_SET_DIS";

    private final View view;

    public SettingDisplay(View view) {
        this.view = view;
    }

    @Override
    public void onSuccess(T value) {
        if(view instanceof EditText) {
            ((EditText) this.view).setText(value.toString());
            Log.d(TAG,"View: "+view.toString()+" Value: "+value.toString());
        }
    }

    @Override
    public void onFailure(DeviceConnectionException e) {
        Log.e(TAG, e.toString());
        Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
