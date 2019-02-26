package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_balance);

        new Handler().post(() -> {
            IrohaHandler.getInstance().addAsset("100000");
            mTextMessage.setText(String.valueOf(IrohaHandler.getInstance().getBalance()));
        });

    }

}
