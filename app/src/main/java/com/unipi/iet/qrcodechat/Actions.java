package com.unipi.iet.qrcodechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.unipi.iet.utility.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

/**
 *  Class related to the activity Actions that shows 2 different actions: creation of the QR code
 *  and scanning of the QR code.
 *
 */
public class Actions extends AppCompatActivity {

    private Button createQRCodeButton;
    private Button scanQRCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actions);
        setSupportActionBar(toolbar);

        //Two actions to do in this activity: create a QR code or scan a QR code
        createQRCodeButton = findViewById(R.id.btnCreateQRCode);
        scanQRCodeButton = findViewById(R.id.btnScanQRCode);

        //When clicked on a create QRCode, user is sent to the CreateQRCodeActivity
        createQRCodeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(Actions.this, CreateQRCode.class);
                i.putExtra("randomkey", Constants.myKey);
                startActivity(i);
            }
        });

        //When clicked on a scanQRCode, user starts the scanning
        scanQRCodeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //Is used an Integrator object to handle the scanning
                IntentIntegrator integrator = new IntentIntegrator(Actions.this);
                integrator.initiateScan();
            }
        });

    }

    //Function called on the success of the scanning of the QRCode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        //Checks if the class instance to use QR code scanning is not null
        if (intentResult != null) {
            //If the result has a null content
            if (intentResult.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            //If the result is not null
            } else {
                Toast.makeText(this, "Scanned", Toast.LENGTH_LONG).show();

                String[] arrExchange = intentResult.getContents().split("=");
                Constants.hisKey = arrExchange[0];
                Constants.exchangeUsername = arrExchange[1];

                //This is in the case in which the constants are correctly set and different from zero
                if ((!Constants.hisKey.isEmpty())&&(!Constants.myKey.isEmpty())){

                    String url = "https://qrcodechat-ca31a.firebaseio.com/exchanges.json";

                    //A GET request is created
                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                        @Override
                        public void onResponse(String s) {

                            //A Firebase reference is created to eventually store the exchange information
                            Firebase reference = new Firebase("https://qrcodechat-ca31a.firebaseio.com/exchanges");

                            Utils u = new Utils();
                            String finalKey = u.xorKeys(Constants.myKey, Constants.hisKey);
                            String[] finalKeyArr = finalKey.split("=");
                            finalKey = finalKeyArr[0];

                            //This is the case in which the user is not in the exchange list
                            if(s.equals("null")) {
                                HashMap<String, String> keyEntry = new HashMap<>();
                                keyEntry.put("key", finalKey);

                                reference.child(UserDetails.username).child(Constants.exchangeUsername).setValue(keyEntry);
                                Toast.makeText(Actions.this, "Key exchange successful", Toast.LENGTH_LONG).show();

                                Intent i = new Intent(Actions.this, Users.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            //This is the case in which the user is already in the exchange list
                            }else{
                                try {
                                    JSONObject obj = new JSONObject(s);

                                    HashMap<String, String> keyEntry = new HashMap<>();
                                    keyEntry.put("key", finalKey);

                                    reference.child(UserDetails.username).child(Constants.exchangeUsername).setValue(keyEntry);
                                    Toast.makeText(Actions.this, "Key exchange successful", Toast.LENGTH_LONG).show();

                                    Intent i = new Intent(Actions.this, Users.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                    },new Response.ErrorListener(){ //This handles request errors
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError );
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(Actions.this);
                    rQueue.add(request);
                }

            }
        }
    }
}
