package com.unipi.iet.qrcodechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import java.io.File;
import android.os.Environment;
import java.security.spec.RSAPrivateKeySpec;
import java.security.KeyFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *  This class belongs to the activity register and handles the registration of a new user.
 */

public class Register extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private EditText username, password;
    private Button registerButton;
    private TextView login;
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
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_register);
        setSupportActionBar(toolbar);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        registerButton = (Button)findViewById(R.id.registerButton);
        login = (TextView)findViewById(R.id.login);

        //Firebase service instantiation
        Firebase.setAndroidContext(this);

        //Event thrown when a user wants to log in the application
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        //Event thrown whenever a user starts a registration giving a username and a password
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Obtain all the needed information from the fields
                user = username.getText().toString();
                pass = password.getText().toString();

                //Password and username validation
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
                    //This variable is used to perform a loading during the registration
                    final ProgressDialog pd = new ProgressDialog(Register.this);
                    pd.setMessage("Loading...");
                    pd.show();

                    //Address that explains the location of the user login information
                    String url = "https://qrcodechat-ca31a.firebaseio.com/users.json";

                    //A GET request to the address above is created
                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){

                        //This method handles the actions to be performed when a response to the request is received
                        @Override
                        public void onResponse(String s) {
                            Firebase reference = new Firebase("https://qrcodechat-ca31a.firebaseio.com/users");

                            String key = "";
                            Utils u = new Utils();
                            try{
                                key = u.generateRandomizedString128Bits();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            try{
                                if(s.equals("null")) {
                                    try {
                                        //RSA keys creation
                                        ae.getRSAKeys();

                                        Utils u2 = new Utils();

                                        //Verify if the permission to write on external storage is granted
                                        if (u2.isExternalStorageWritable()){
                                            KeyFactory fact = KeyFactory.getInstance("RSA");
                                            RSAPrivateKeySpec priv = fact.getKeySpec(ae.getPrKey(),
                                                                        RSAPrivateKeySpec.class);
                                            //Save the private key on internal storage as a file
                                            File newfile = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME +"_"+ user + PRIVATE_KEY_EXTENSION);
                                            Utils.saveToFile(newfile, priv.getModulus(), priv.getPrivateExponent());

                                            //Saving the password on Firebase database
                                            reference.child(user).child("password").setValue(ae.encryptAsymmetric(pass.getBytes(), ae.getPbKey()));

                                            Constants.myKey = key;
                                            reference.child(user).child("key").setValue(key);

                                            //Saving the public key on Firebase database
                                            reference.child(user).child("publicKey").setValue(ae.getPublicKey());

                                            //Notify the user that the registration succeeded
                                            Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_LONG).show();
                                        }else{
                                            //Notify when the external storage is not available
                                            Toast.makeText(Register.this, "External storage not available", Toast.LENGTH_LONG).show();
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    try {
                                        //A json object is created from the result stored inside the string
                                        JSONObject obj = new JSONObject(s);

                                        if (!obj.has(user)) {
                                            try {
                                                //Creation of the RSA keys
                                                ae.getRSAKeys();

                                                Utils u2 = new Utils();

                                                //Verification of the external storage permission to write
                                                if (u2.isExternalStorageWritable()){

                                                    //Private key generation
                                                    KeyFactory fact = KeyFactory.getInstance("RSA");
                                                    RSAPrivateKeySpec priv = fact.getKeySpec(ae.getPrKey(),
                                                            RSAPrivateKeySpec.class);
                                                    //Create new file for the private key
                                                    File newfile = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME+"_"+user + PRIVATE_KEY_EXTENSION);
                                                    Utils.saveToFile( newfile, priv.getModulus(), priv.getPrivateExponent());

                                                    Constants.myKey = key;

                                                    //Save the values into the Firebase Database
                                                    reference.child(user).child("password").setValue((ae.encryptAsymmetric(pass.getBytes(), ae.getPbKey())));
                                                    reference.child(user).child("key").setValue(key);
                                                    reference.child(user).child("publicKey").setValue(ae.getPublicKey());

                                                    //Notify user that the registration finished with success
                                                    Toast.makeText(Register.this, "registration successful", Toast.LENGTH_LONG).show();
                                                }else{
                                                    //Notify that the external storage is not available
                                                    Toast.makeText(Register.this, "External storage not available", Toast.LENGTH_LONG).show();
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            //Notify the user that this username chosen already exists
                                            Toast.makeText(Register.this, "username already exists", Toast.LENGTH_LONG).show();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            pd.dismiss();
                        }

                    },new Response.ErrorListener(){ //To handle errors of the request
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
