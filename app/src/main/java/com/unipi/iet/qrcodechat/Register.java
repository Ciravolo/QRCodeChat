package com.unipi.iet.qrcodechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

public class Register extends AppCompatActivity {
    EditText username, password;
    Button registerButton;
    String user, pass;
    TextView login;
    AsymmetricEncryption ae = new AsymmetricEncryption();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        registerButton = (Button)findViewById(R.id.registerButton);
        login = (TextView)findViewById(R.id.login);

        Firebase.setAndroidContext(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();

                if(user.equals("")){
                    username.setError("can't be blank");
                }
                else if(pass.equals("")){
                    password.setError("can't be blank");
                }
                else if(!user.matches("[A-Za-z0-9]+")){
                    username.setError("only alphabet or number allowed");
                }
                else if(user.length()<5){
                    username.setError("at least 5 characters long");
                }
                else if(pass.length()<5){
                    password.setError("at least 5 characters long");
                }
                else {
                    final ProgressDialog pd = new ProgressDialog(Register.this);
                    pd.setMessage("Loading...");
                    pd.show();

                    String url = "https://qrcodechat-ca31a.firebaseio.com/users.json";

                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                        @Override
                        public void onResponse(String s) {
                            Firebase reference = new Firebase("https://qrcodechat-ca31a.firebaseio.com/users");

                            //create new key for user

                            String key = "";
                            Utils u = new Utils();
                            try{
                                key = u.generateRandomizedString128Bits();
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                            if(s.equals("null")) {
                                try {
                                    //Creazione delle chiavi
                                    ae.getRSAKeys();

                                    Utils u2 = new Utils();
                                    if (u2.isExternalStorageWritable()){
                                        u2.writeFileWithContent("privatekey.txt", ae.getPrivateKey());
                                        Log.i("privatekey: ", ae.getPrivateKey());
                                    }else{
                                        Toast.makeText(Register.this, "External storage not available", Toast.LENGTH_LONG).show();
                                    }
                                    Log.i("public key:", ae.getPublicKey());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                System.out.println("Registro nuovo");
                                //Salvataggio della password criptata sul database
                                reference.child(user).child("password").setValue(ae.encryptAsymmetric(pass.getBytes(), ae.getPbKey()));
                                Constants.myKey = key;
                                reference.child(user).child("key").setValue(key);
                                //Salvataggio della chiave pubblica nel database
                                reference.child(user).child("publicKey").setValue(ae.getPublicKey());

                                Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(Register.this, "Username already exists", Toast.LENGTH_LONG).show();
                            }
                            pd.dismiss();
                        }

                    },new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError );
                            pd.dismiss();
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(Register.this);
                    rQueue.add(request);
                }
            }
        });
    }
}
