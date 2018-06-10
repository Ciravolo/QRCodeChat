package com.unipi.iet.qrcodechat;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
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
 * Class that handles functionalities in ChatActivity.
 */
public class Chat extends AppCompatActivity {

    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2, reference3;
    AsymmetricEncryption ae = new AsymmetricEncryption();
    String PRIVATE_KEY_FILENAME = "privatekey";
    String PRIVATE_KEY_EXTENSION = ".txt";

    /**
     * Creation of the ChatActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        //Two needed queries: chat messages I send and chat messages I receive
        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://qrcodechat-ca31a.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://qrcodechat-ca31a.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);
        reference3 = new Firebase("https://qrcodechat-ca31a.firebaseio.com/users");
        //On the send operation of a message
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if (!messageText.equals("")) {

                    String chatWith_publicKey = reference3.child(UserDetails.chatWith).child("publicKey").toString();
                    byte[] publicBytes = new byte[0];
                    try {
                        publicBytes = Hex.decodeHex(chatWith_publicKey.toCharArray());
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        PublicKey pubKey = keyFactory.generatePublic(keySpec);
                        String messageToBeSent = ae.encryptAsymmetric(messageText.getBytes(), pubKey);
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("message", messageToBeSent);
                        map.put("user", UserDetails.username);
                        map.put("flag", Integer.toString(1));
                        reference1.push().setValue(map);
                        reference2.push().setValue(map);
                        messageArea.setText("");
                    } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String key = dataSnapshot.getKey();
                Map<String, Object> map2 = new HashMap<String, Object>();

                String message = map.get("message").toString();

                Utils u = new Utils();
                if (u.isExternalStorageWritable()){
                    File fileToRead = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME + "_"+ UserDetails.username + PRIVATE_KEY_EXTENSION);
                    try{
                        PrivateKey privKeyFromDevice = Utils.readPrivateKey(fileToRead);
                        byte[] toDecrypt = Hex.decodeHex(message.toCharArray());
                        String messageDecrypted = ae.decryptAsymmetric(toDecrypt, privKeyFromDevice);
                        String userName = map.get("user").toString();

                        if (userName.equals(UserDetails.username)) {
                            addMessageBox("You:-\n" + messageDecrypted, 1);
                        } else {
                            addMessageBox(UserDetails.chatWith + ":-\n" + messageDecrypted, 2);
                            map2.put("message", messageDecrypted);
                            map2.put("user", userName);
                            map2.put("flag", Integer.toString(0));
                            reference1.child(key).updateChildren(map2);
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
        reference2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String key = dataSnapshot.getKey();
                Map<String, Object> map2 = new HashMap<String, Object>();
                String message = map.get("message").toString();

                Utils u = new Utils();
                if (u.isExternalStorageWritable()){
                    File fileToRead = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME + "_"+ UserDetails.username + PRIVATE_KEY_EXTENSION);
                    try{
                        PrivateKey privKeyFromDevice = Utils.readPrivateKey(fileToRead);
                        byte[] toDecrypt = Hex.decodeHex(message.toCharArray());
                        String messageDecrypted = ae.decryptAsymmetric(toDecrypt, privKeyFromDevice);
                        String userName = map.get("user").toString();
                        if (!userName.equals(UserDetails.username)) {
                            map2.put("message", messageDecrypted);
                            map2.put("user", userName);
                            map2.put("flag", Integer.toString(0));
                            reference2.child(key).updateChildren(map2);
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
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
