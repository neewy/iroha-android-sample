package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.IOException;

import static com.example.myapplication.Constants.PREFS;
import static com.example.myapplication.Constants.SETTINGS;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private SharedPreferences sPref;
    private boolean areSettingsSet;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_send:
                    startActivity(new Intent(MainActivity.this, SendActivity.class));
                    return true;
                case R.id.navigation_balance:
                    new Handler().post(() -> {
                        if (areSettingsSet)
                            mTextMessage.setText(String.valueOf(IrohaHandler.getInstance().getBalance()));
                    });
                    return true;
                case R.id.navigation_receive:
                    startActivity(new Intent(MainActivity.this, ReceiveActivity.class));
                    return true;
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        areSettingsSet = areSettingsSet();

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_balance);

        new Handler().post(() -> {
            if (areSettingsSet)
                mTextMessage.setText(String.valueOf(IrohaHandler.getInstance().getBalance()));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.iroha_settings:
                startActivity(new Intent(MainActivity.this, IrohaSettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void switchEmptyView() {
        ViewFlipper switcher = (ViewFlipper) findViewById(R.id.flipper);
        if (areSettingsSet) {
            switcher.setDisplayedChild(1);
        } else {
            switcher.setDisplayedChild(0);
        }
    }

    private boolean areSettingsSet() {
        sPref = getSharedPreferences(PREFS, MODE_PRIVATE);
        // dirty hack
        return sPref.getBoolean(SETTINGS, false) || IrohaHandler.account != null;
    }

    @Override
    protected void onResume() {
        switchEmptyView();
        super.onResume();
    }
}
