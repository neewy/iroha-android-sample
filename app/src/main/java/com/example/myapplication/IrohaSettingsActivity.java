package com.example.myapplication;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import io.grpc.ConnectivityState;
import jp.co.soramitsu.iroha.java.IrohaAPI;

import static com.example.myapplication.Constants.PREFS;
import static com.example.myapplication.Constants.QR_CODE;
import static com.example.myapplication.Constants.SCAN_IROHA_SETTINGS;
import static com.example.myapplication.Constants.SETTINGS;
import static com.example.myapplication.Constants.SETTINGS_OBJECT;

public class IrohaSettingsActivity extends AppCompatActivity {

    EditText networkAddress;
    EditText portNumber;
    EditText publicKey;
    EditText privateKey;
    EditText accountId;

    Button connect;

    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iroha_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sPref = getPreferences(MODE_PRIVATE);

        networkAddress = findViewById(R.id.networkAddress);
        portNumber = findViewById(R.id.portNumber);
        publicKey = findViewById(R.id.publicKey);
        privateKey = findViewById(R.id.privateKey);
        accountId = findViewById(R.id.accountId);
        connect = findViewById(R.id.connect);

        FloatingActionButton fab = findViewById(R.id.scanSettings);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IrohaSettingsActivity.this, QrCodeScanner.class);
                startActivityForResult(intent, SCAN_IROHA_SETTINGS);
            }
        });


        connect.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                tryAndSaveSettings(getInput());
                finish();
            }
        });
    }

    private IrohaSettingsMessage getInput() {
        return new IrohaSettingsMessage(
                networkAddress.getText().toString(),
                portNumber.getText().toString(),
                publicKey.getText().toString(),
                privateKey.getText().toString(),
                accountId.getText().toString());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SCAN_IROHA_SETTINGS) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String irohaSettings = data.getStringExtra(QR_CODE);
                IrohaSettingsMessage message = null;
                try {
                    message = (IrohaSettingsMessage) IrohaSettingsMessage.fromString(irohaSettings);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (message != null) {
                    networkAddress.setText(message.networkAddress, TextView.BufferType.EDITABLE);
                    portNumber.setText(message.portNumber, TextView.BufferType.EDITABLE);
                    publicKey.setText(message.publicKey, TextView.BufferType.EDITABLE);
                    privateKey.setText(message.privateKey, TextView.BufferType.EDITABLE);
                    accountId.setText(message.accountId, TextView.BufferType.EDITABLE);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void tryAndSaveSettings(IrohaSettingsMessage settingsMessage) {
        IrohaAPI api;
        try {
            api = new IrohaAPI(settingsMessage.networkAddress, Integer.parseInt(settingsMessage.portNumber));
        }
        catch (NumberFormatException e) {
            return;
        }
        // does not work for probing:
        // ConnectivityState state = api.getChannel().getState(true);
        IrohaHandler.setApi(api);
        IrohaHandler.setAccount(settingsMessage);
        saveSettings(settingsMessage);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveSettings(IrohaSettingsMessage settingsMessage) {
        sPref = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        try {
            ed.putString(SETTINGS_OBJECT, IrohaSettingsMessage.toString(settingsMessage));
        } catch (IOException e) {
            ed.putBoolean(SETTINGS, false);
        }
        ed.putBoolean(SETTINGS, true);
        ed.commit();
    }

}
