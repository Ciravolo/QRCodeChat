package com.unipi.iet.qrcodechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.zxing.integration.android.IntentIntegrator;

public class Actions extends AppCompatActivity {

    Button createQRCodeButton;
    Button scanQRCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);

        createQRCodeButton = findViewById(R.id.btnCreateQRCode);
        scanQRCodeButton = findViewById(R.id.btnScanQRCode);

        createQRCodeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Actions.this, CreateQRCode.class));
            }
        });


        scanQRCodeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                IntentIntegrator integrator = new IntentIntegrator(Actions.this);
                integrator.initiateScan();
            }
        });

    }
}
