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
import java.io.File;
import android.os.Environment;
import java.security.spec.RSAPrivateKeySpec;
import java.security.KeyFactory;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author joana
 * Class that belongs to the activity: Register Activity
 *
 * Handles the registration of a new user.
 *
 */
public class Register extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    EditText username, password;
    Button registerButton;
    String user, pass;
    TextView login;
    AsymmetricEncryption ae = new AsymmetricEncryption();
    String PRIVATE_KEY_FILENAME = "privatekey";
    String PRIVATE_KEY_EXTENSION = ".txt";

    static final Integer CAMERA = 0x1;
    static final Integer WRITE_EXST = 0x2;
    static final Integer READ_EXST = 0x3;

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

        Firebase.setAndroidContext(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        askForPermission(Manifest.permission.CAMERA,CAMERA);
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
                            try{
                                if(s.equals("null")) {
                                    try {
                                        //Creazione delle chiavi
                                        ae.getRSAKeys();

                                        Utils u2 = new Utils();
                                        if (u2.isExternalStorageWritable()){
                                            KeyFactory fact = KeyFactory.getInstance("RSA");
                                            RSAPrivateKeySpec priv = fact.getKeySpec(ae.getPrKey(),
                                                                        RSAPrivateKeySpec.class);

                                            Log.i("create the new file:", PRIVATE_KEY_FILENAME+"_"+user + PRIVATE_KEY_EXTENSION);
                                            File newfile = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME +"_"+ user + PRIVATE_KEY_EXTENSION);
                                            Utils.saveToFile(newfile, priv.getModulus(), priv.getPrivateExponent());
                                            System.out.println("Registro nuovo");
                                            //Saving the password on Firebase database
                                            reference.child(user).child("password").setValue(ae.encryptAsymmetric(pass.getBytes(), ae.getPbKey()));
                                            Constants.myKey = key;
                                            reference.child(user).child("key").setValue(key);
                                            //Saving the public key on Firebase database
                                            reference.child(user).child("publicKey").setValue(ae.getPublicKey());
                                            Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(Register.this, "External storage not available", Toast.LENGTH_LONG).show();
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    try {
                                        JSONObject obj = new JSONObject(s);
                                        if (!obj.has(user)) {
                                            try {
                                                //Creation of the RSA keys
                                                ae.getRSAKeys();

                                                Utils u2 = new Utils();
                                                if (u2.isExternalStorageWritable()){
                                                    KeyFactory fact = KeyFactory.getInstance("RSA");
                                                    RSAPrivateKeySpec priv = fact.getKeySpec(ae.getPrKey(),
                                                            RSAPrivateKeySpec.class);
                                                    File newfile = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME+"_"+user + PRIVATE_KEY_EXTENSION);
                                                    Utils.saveToFile( newfile, priv.getModulus(), priv.getPrivateExponent());
                                                    reference.child(user).child("password").setValue((ae.encryptAsymmetric(pass.getBytes(), ae.getPbKey())));
                                                    Constants.myKey = key;
                                                    reference.child(user).child("key").setValue(key);
                                                    reference.child(user).child("publicKey").setValue(ae.getPublicKey());
                                                    Toast.makeText(Register.this, "registration successful", Toast.LENGTH_LONG).show();
                                                }else{
                                                    Toast.makeText(Register.this, "External storage not available", Toast.LENGTH_LONG).show();
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            switch (requestCode) {
                case 1:
                    askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXST);
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


    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(Register.this, permission) != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(Register.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(Register.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(Register.this, new String[]{permission}, requestCode);
            }
        }
    }
}
