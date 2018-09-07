package com.estimote.configuration;

import android.app.Application;
import com.estimote.coresdk.common.config.EstimoteSDK;

public class ConfigurationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EstimoteSDK.initialize(getApplicationContext(), BuildConfig.Estimote_ApiId, BuildConfig.Estimote_ApiToken);
        EstimoteSDK.enableDebugLogging(true);
    }
}
