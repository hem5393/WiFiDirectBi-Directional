package com.example.me.wifidirectbi_directional;

import android.content.Context;

/**
 * Created by ME on 05-Dec-16.
 */
import android.content.Context;

public class GlobalApplication extends android.app.Application{
    private static Context GlobalContext;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        if(GlobalApplication.GlobalContext == null){
            GlobalApplication.GlobalContext = getApplicationContext();
        }
    }

    public static Context getGlobalAppContext() {
        return GlobalApplication.GlobalContext;
    }
}