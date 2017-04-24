package app.earplug.com.earplugapp;

import android.app.Application;

import com.yotadevices.util.LogCat;


public class EarPlugApplication extends Application {

    private static final String TAG = "EarPlug";

    @Override
    public void onCreate() {
        super.onCreate();
        LogCat.init(TAG, true);
    }
}
