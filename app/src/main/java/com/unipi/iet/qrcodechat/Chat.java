package com.unipi.iet.qrcodechat;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.unipi.iet.utility.AsymmetricEncryption;
import com.unipi.iet.utility.Utils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that handles chat functionalities and shows all messages exchanged by sender and receiver
 */
public class Chat extends AppCompatActivity {

    private LinearLayout layout;
    private RelativeLayout layout_2;
    private ImageView sendButton;
    private EditText messageArea;
    private ScrollView scrollView;
    private Firebase referenceUsernameChatWith, referenceChatWithUsername;

    //Structure that handles security functionalities
    private AsymmetricEncryption ae = new AsymmetricEncryption();

    //Private Key filename
    private String PRIVATE_KEY_FILENAME = "privatekey";
    private String PRIVATE_KEY_EXTENSION = ".txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        //Firebase service instantiation
        Firebase.setAndroidContext(this);

        //References to the address in which are located the messages exchanged by sender and receiver
        referenceUsernameChatWith = new Firebase("https://qrcodechat-ca31a.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        referenceChatWithUsername = new Firebase("https://qrcodechat-ca31a.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        //Handling of message sending action
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Text is made from the message area
                String messageText = messageArea.getText().toString();

                //This is the case in which the message is an empty string
                if (!messageText.equals("")) {

                    //This address is used to obtain the location of the chat_with user public key
                    String url_chatWith = "https://qrcodechat-ca31a.firebaseio.com/users/" + UserDetails.username + ".json";

                    //A GET request to the address above is created
                    StringRequest request_chatWith = new StringRequest(Request.Method.GET, url_chatWith, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            try {
                                //A json object is created from the result stored inside the string
                                JSONObject obj = new JSONObject(s);

                                //The public key is stored in a string variable
                                String chatWith_publicKey = obj.getString("publicKey");

                                try {
                                    //The message is encrypted using the public key obtained
                                    byte[] publicBytes = Hex.decodeHex(chatWith_publicKey.toCharArray());
                                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
                                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                    PublicKey pubKey = keyFactory.generatePublic(keySpec);
                                    String messageToBeSent = ae.encryptAsymmetric(messageText.getBytes(), pubKey);

                                    //An HashMap object containing all the information is created
                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put("message", messageToBeSent);
                                    map.put("user", UserDetails.username);

                                    //This flag enables the notification for this message
                                    map.put("flag", Integer.toString(1));

                                    //The object is pushed to the Firebase reference
                                    referenceUsernameChatWith.push().setValue(map);
                                    messageArea.setText("");

                                } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                                    e.printStackTrace();
                                }
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() { //To handle errors of the request
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError);
                        }
                    });

                    //This address is used to obtain the location of the current user public key
                    String url_user = "https://qrcodechat-ca31a.firebaseio.com/users/" + UserDetails.chatWith + ".json";

                    //A GET request to the address above is created
                    StringRequest request_user = new StringRequest(Request.Method.GET, url_user, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            try {
                                //A json object is created from the result stored inside the string
                                JSONObject obj = new JSONObject(s);

                                //The public key is stored in a string variable
                                String user_publicKey = obj.getString("publicKey");

                                try {

                                    //The message is encrypted using the public key obtained
                                    byte[] publicBytes = Hex.decodeHex(user_publicKey.toCharArray());
                                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
                                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                    PublicKey pubKey = keyFactory.generatePublic(keySpec);
                                    String messageToShow = ae.encryptAsymmetric(messageText.getBytes(), pubKey);

                                    //An HashMap object containing all the information is created
                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put("message", messageToShow);
                                    map.put("user", UserDetails.username);
                                    map.put("flag", Integer.toString(1));

                                    //The object is pushed to the Firebase reference
                                    referenceChatWithUsername.push().setValue(map);
                                    messageArea.setText("");

                                } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                                    e.printStackTrace();
                                }
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() { //To handle errors of the request
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError);
                        }
                    });
                    RequestQueue rQueue1 = Volley.newRequestQueue(Chat.this);
                    rQueue1.add(request_chatWith);

                    RequestQueue rQueue2 = Volley.newRequestQueue(Chat.this);
                    rQueue2.add(request_user);
                }
            }
        });

        //Here is handled the case of a message received and that must be decrypted before to be
        //printed in the screen
        referenceUsernameChatWith.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String key = dataSnapshot.getKey();
                Map<String, Object> map2 = new HashMap<String, Object>();

                //The message is stored inside a string variable
                String message = map.get("message").toString();

                Utils u = new Utils();

                //The possibility to write in external storage must be verified
                if (u.isExternalStorageWritable()){
                    File fileToRead = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME + "_"+ UserDetails.username + PRIVATE_KEY_EXTENSION);
                    try{
                        //The private key is read from the file
                        PrivateKey privKeyFromDevice = Utils.readPrivateKey(fileToRead);

                        //The message is decrypted and stored inside a string variable
                        byte[] toDecrypt = Hex.decodeHex(message.toCharArray());
                        String messageDecrypted = ae.decryptAsymmetric(toDecrypt, privKeyFromDevice);

                        String userName = map.get("user").toString();

                        //Here is the case of a message written by the current user. It is
                        //sufficient to print the message
                        if (userName.equals(UserDetails.username)) {
                            addMessageBox(messageDecrypted, 1);
                        //This is the case of a messsage received by the chat_with user. In this
                        //case the message has been read and the notification for this message must
                        //be disabled (the flag must be set to zero)
                        } else {
                            addMessageBox(messageDecrypted, 2);
                            map2.put("message", message);
                            map2.put("user", userName);
                            map2.put("flag", Integer.toString(0));
                            referenceUsernameChatWith.child(key).updateChildren(map2);
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(Chat.this, "External storage not available", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Here is handled the case of a message sent that only must be written in the screen
        referenceChatWithUsername.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String key = dataSnapshot.getKey();
                Map<String, Object> map2 = new HashMap<String, Object>();

                //The message is stored inside a string variable
                String message = map.get("message").toString();

                String userName = map.get("user").toString();

                //This is the case of a messsage sent by the chat_with user. In this
                //case the message has been read and the notification for this message must be
                //disabled (the flag must be set to zero)
                if (!userName.equals(UserDetails.username)) {
                    map2.put("message", message);
                    map2.put("user", userName);
                    map2.put("flag", Integer.toString(0));
                    referenceChatWithUsername.child(key).updateChildren(map2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    //This method is used to add the message box
    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText("\n" + message + "\n");
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        //This is the case of a message written by current user
        if(type == 1) {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundColor(getResources().getColor(R.color.chatMe));
            textView.setTextColor(getResources().getColor(R.color.black));
        }
        //This is the case of a message written by chat_with user
        else{
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundColor(getResources().getColor(R.color.chatFrom));
            textView.setTextColor(getResources().getColor(R.color.black));
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
