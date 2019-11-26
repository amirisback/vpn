package com.frogobox.evpn;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {

    private static BaseApplication instance;

    public static String getResourceString(int resId) {
        return instance.getString(resId);
    }

    public static BaseApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

}
