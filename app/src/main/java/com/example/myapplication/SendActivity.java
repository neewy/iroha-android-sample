package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import static com.example.myapplication.Constants.QR_CODE;
import static com.example.myapplication.Constants.SCAN_TRANSFER;

public class SendActivity extends AppCompatActivity {

    EditText accountId;
    EditText numberOfAssets;
    EditText transferMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        accountId = findViewById(R.id.accountId);
        numberOfAssets = findViewById(R.id.numberOfAssets);
        transferMessage = findViewById(R.id.transferMessage);
        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v ->
        {
            IrohaHandler.getInstance().sendAsset(
                    String.valueOf(accountId.getText()),
                    String.valueOf(transferMessage.getText()),
                    String.valueOf(numberOfAssets.getText())
            );
            this.finish();
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(SendActivity.this, QrCodeScanner.class);
            startActivityForResult(intent, SCAN_TRANSFER);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SCAN_TRANSFER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String qrCodeMessage = data.getStringExtra(QR_CODE);
                QrCodeMessage message = null;
                try {
                    message = (QrCodeMessage) QrCodeMessage.fromString(qrCodeMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (message != null) {
                    accountId.setText(message.accountName, TextView.BufferType.EDITABLE);
                    numberOfAssets.setText(message.numberOfAssets, TextView.BufferType.EDITABLE);
                }
            }
        }
    }

}
