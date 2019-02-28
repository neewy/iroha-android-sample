package com.example.myapplication;

import android.app.Application;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.IOException;

import jp.co.soramitsu.iroha.java.IrohaAPI;

import static com.example.myapplication.Constants.PREFS;
import static com.example.myapplication.Constants.SETTINGS;
import static com.example.myapplication.Constants.SETTINGS_OBJECT;

public class MyApplication extends Application {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        try {
            restoreConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void restoreConnection() throws IOException, ClassNotFoundException {
        SharedPreferences sPref = getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (sPref.getBoolean(SETTINGS, false)) {
            IrohaSettingsMessage message = (IrohaSettingsMessage) IrohaSettingsMessage
                    .fromString(sPref.getString(SETTINGS_OBJECT, "none"));
            IrohaHandler.setApi(new IrohaAPI(message.networkAddress, Integer.parseInt(message.portNumber)));
            IrohaHandler.setAccount(message);
        }
    }

    @Override
    public void onTerminate() {
        IrohaHandler.terminateChannel();
        super.onTerminate();
    }
}
