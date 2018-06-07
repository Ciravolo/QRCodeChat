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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class Login extends AppCompatActivity {
    TextView registerUser;
    EditText username, password;
    Button loginButton;
    String user, pass;
    AsymmetricEncryption ae = new AsymmetricEncryption();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        registerUser = (TextView)findViewById(R.id.register);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        loginButton = (Button)findViewById(R.id.loginButton);
        Firebase.setAndroidContext(this);

        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick (View v){
                user = username.getText().toString();
                pass = password.getText().toString();


                if (user!=null){
                    if (user.equals("")) {
                        username.setError("can't be blank");
                    } else if (pass.equals("")) {
                        password.setError("can't be blank");
                    } else {
                        final ProgressDialog pd = new ProgressDialog(Login.this);
                        pd.setMessage("Loading...");
                        pd.show();
                        String url = "https://qrcodechat-ca31a.firebaseio.com/users.json";
                        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                if (s.equals("null")) {
                                    Toast.makeText(Login.this, "user not found", Toast.LENGTH_LONG).show();
                                } else {
                                    try {
                                        JSONObject obj = new JSONObject(s);

                                        if (!obj.has(user)) {
                                            Toast.makeText(Login.this, "user not found", Toast.LENGTH_LONG).show();
                                        } else {
                                            Utils u2 = new Utils();
                                            if (u2.isExternalStorageWritable()) {
                                                String privateKeyContent = u2.readContentFromFile("privatekey.txt");
                                                Log.i("password:", privateKeyContent);
                                                PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKeyContent.getBytes("UTF-8"), Base64.DEFAULT));
                                                KeyFactory kf = KeyFactory.getInstance("RSA");
                                                PrivateKey privateKey = kf.generatePrivate(keySpecPKCS8);
                                                String passDecrypted = ae.decryptAsymmetric(obj.getJSONObject(user).getString("password").getBytes("UTF-8"), privateKey);
                                                if (passDecrypted.equals(pass)) {
                                                    UserDetails.username = user;
                                                    UserDetails.password = pass;
                                                    Constants.myKey = obj.getJSONObject(user).getString("key");

                                                    Log.i("my key on login:", Constants.myKey);

                                                    startActivity(new Intent(Login.this, Users.class));
                                                } else {
                                                    Toast.makeText(Login.this, "incorrect password", Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                Toast.makeText(Login.this, "External storage not available", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } catch (JSONException | UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                                        e.printStackTrace();
                                    }
                                }

                                pd.dismiss();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                System.out.println("" + volleyError);
                                pd.dismiss();
                            }
                        });

                        RequestQueue rQueue = Volley.newRequestQueue(Login.this);
                        rQueue.add(request);
                    }
                }
                else{
                    Toast.makeText(Login.this, "user not found", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}