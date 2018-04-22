package com.unipi.iet.qrcodechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 *  Class related to the activity Actions that shows 2 different actions: creation of the qr code
 *  and scanning of the qr code.
 *
 */
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Log.d("ActionsActivity", "Cancelled");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();

            } else {
                Log.d("ActionsActivity", "Scanned");
                Toast.makeText(this, "Scanned: " + intentResult.getContents(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
