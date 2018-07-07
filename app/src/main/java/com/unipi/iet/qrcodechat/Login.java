package com.unipi.iet.qrcodechat;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.unipi.iet.utility.AsymmetricEncryption;
import com.unipi.iet.utility.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.PrivateKey;
import java.io.File;
import android.os.Environment;
import org.apache.commons.codec.binary.Hex;

/**
 *  This class belongs to the activity login and handles the autentication of users. It also
 *  allows the possibility to ask for the sign-in of a new user.
 */

public class Login extends AppCompatActivity {

    private TextView registerUser;
    private EditText username, password;
    private Button loginButton;
    private String user = "";
    private String pass = "";
    //Structure that handles security functionalities
    private AsymmetricEncryption ae = new AsymmetricEncryption();
    //Private Key filename
    private String PRIVATE_KEY_FILENAME = "privatekey";
    private String PRIVATE_KEY_EXTENSION = ".txt";
    //Permissions
    private static final Integer CAMERA = 0x1;
    private static final Integer WRITE_EXST = 0x2;
    private static final Integer READ_EXST = 0x3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);

        registerUser = (TextView)findViewById(R.id.register);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        loginButton = (Button)findViewById(R.id.loginButton);

        //Firebase service instantiation
        Firebase.setAndroidContext(this);

        //Event thrown when a user wants to start a new registration
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Register activity is started
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        //This method enables the permissions required for the application starting from camera
        //The others will be required only if this permission is accepted by the user
        askForPermission(Manifest.permission.CAMERA,CAMERA);

        //Event thrown when a user has inserted username and password and has pressed the button
        //in order to perform login action
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick (View v){
                //Username and password are stored inside these class attributes
                user = username.getText().toString();
                pass = password.getText().toString();

                //This condiction verifies the presence of the username
                if (user!=null){
                    if (user.equals("")) {
                        username.setError("can't be blank");
                    } else if (pass.equals("")) {
                        password.setError("can't be blank");
                    } else {
                        //This variable is used to perform a loading during the login
                        final ProgressDialog pd = new ProgressDialog(Login.this);
                        pd.setMessage("Loading...");
                        pd.show();

                        //Address that explains the location of the user login information
                        String url = "https://qrcodechat-ca31a.firebaseio.com/users.json";
                        //A GET request to the address above is created
                        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

                            //This method handles the actions to be performed when a response to the request is received
                            @Override
                            public void onResponse(String s) {
                                if (s.equals("null")) {
                                    Toast.makeText(Login.this, "user not found", Toast.LENGTH_LONG).show();
                                } else {
                                    try {

                                        //A json object is created from the result stored inside the string
                                        JSONObject obj = new JSONObject(s);

                                        if (!obj.has(user)) {
                                            Toast.makeText(Login.this, "user not found", Toast.LENGTH_LONG).show();
                                        } else {
                                            Utils u2 = new Utils();

                                            //The possibility to write in external storage must be verified
                                            if (u2.isExternalStorageWritable()) {

                                                //The private key must be read from the file privateKey_Username.txt
                                                File fileToRead = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME + "_"+ user + PRIVATE_KEY_EXTENSION);

                                                 try{

                                                     //The password is decrypted using the private key read
                                                     PrivateKey privKeyFromDevice = Utils.readPrivateKey(fileToRead);
                                                     byte[] toDecrypt = Hex.decodeHex(obj.getJSONObject(user).getString("password").toCharArray());
                                                     String passDecrypted = ae.decryptAsymmetric(toDecrypt, privKeyFromDevice);

                                                     //The password decrypted must be compared with password inserted by the user
                                                     if (passDecrypted.equals(pass)) {
                                                         UserDetails.username = user;
                                                         UserDetails.password = pass;
                                                         Constants.myKey = obj.getJSONObject(user).getString("key");
                                                         startActivity(new Intent(Login.this, Users.class));
                                                     } else {
                                                         Toast.makeText(Login.this, "incorrect password", Toast.LENGTH_LONG).show();
                                                     }
                                                 }
                                                 catch(Exception e){
                                                    e.printStackTrace();
                                                 }
                                            } else {
                                                Toast.makeText(Login.this, "External storage not available", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                pd.dismiss();
                            }
                        }, new Response.ErrorListener() { //To handle errors of the request
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                System.out.println("" + volleyError);
                                pd.dismiss();
                            }
                        });

                        //The request is added to the queue and is sent to Firebase server
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

    //Function that ask the storage permissions of the application
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            switch (requestCode) {
                case 1:
                    askForPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXST);
                    break;
                case 2:
                    askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);
                    break;
                case 3:
                    break;
            }
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    //This function is called to grant a permission asked
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(Login.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Login.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(Login.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(Login.this, new String[]{permission}, requestCode);
            }
        }
    }
}