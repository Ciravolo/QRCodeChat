package com.unipi.iet.qrcodechat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Class that belongs to the UserActivity
 * Handles the list of users that appears, this list is composed of all the users which a user have done
 * an exchange of a QRCode.
 */
public class Users extends AppCompatActivity {

    ListView usersList;
    TextView noUsersText;
    ArrayList<String> al = new ArrayList<>();
    ArrayList<Firebase> references = new ArrayList<>();
    int totalUsers = 0;
    ProgressDialog pd;
    int i;
    String PRIVATE_KEY_FILENAME = "privatekey";
    String PRIVATE_KEY_EXTENSION = ".txt";
    AsymmetricEncryption ae = new AsymmetricEncryption();


    /**
     * Creation of the UsersActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_users);
        setSupportActionBar(toolbar);

        //Initializing variables
        usersList = (ListView)findViewById(R.id.usersList);
        noUsersText = (TextView)findViewById(R.id.noUsersText);

        //Setting up the context
        Firebase.setAndroidContext(this);

        pd = new ProgressDialog(Users.this);
        pd.setMessage("Loading...");
        pd.show();

        //Url to be queried to check all the users to which I have done the exchange with
        String url = "https://qrcodechat-ca31a.firebaseio.com/exchanges/"+UserDetails.username+".json";

        //Obtain all the users from the database doing this request
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            //in case of success, call doOnSuccess method
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        },new Response.ErrorListener(){
            //When there has been an error to the queried database
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(Users.this);
        rQueue.add(request);

        //When the user clicks on a specific user on his list chat
        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Start a chat with this user, UserDetails.chatWith is the name of the user to chat with
                  and by doing al.get(position) I obtain the user on the position of the list in which I
                  clicked.
                */
                UserDetails.chatWith = al.get(position);
                startActivity(new Intent(Users.this, Chat.class));
            }
        });

        //When a user in the list is pressed for a long time with the intention of being deleted from the list.
        usersList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final CharSequence[] items = {"Delete"};

                AlertDialog.Builder builder = new AlertDialog.Builder(Users.this);

                builder.setTitle("Action:");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        ArrayList<String> url = new ArrayList<>();
                        url.add("https://qrcodechat-ca31a.firebaseio.com/exchanges/"+UserDetails.username+"/"+al.get(position)+".json");
                        url.add("https://qrcodechat-ca31a.firebaseio.com/exchanges/"+al.get(position)+"/"+UserDetails.username+".json");
                        url.add("https://qrcodechat-ca31a.firebaseio.com/messages/"+UserDetails.username+"_"+al.get(position)+".json");
                        url.add("https://qrcodechat-ca31a.firebaseio.com/messages/"+al.get(position)+"_"+UserDetails.username+".json");
                        al.clear();
                        for(int i = 0; i != url.size(); ++i) {
                            try {
                                //a delete request is done to all of the urls mentioned before.
                                deleteRequest(url.get(i));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        new AlertDialog.Builder(Users.this)
                                .setTitle("Success")
                                .setMessage("Chat deleted")
                                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(getApplicationContext(), Users.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }).show();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
    }

    /**
     * Create the options on the menu, this is in order to do a new exchange
     * @param menu Menu of the app in the right side of the activity
     * @return bool: whether the menu has been created or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle a DELETE request to a specific url
     * @param url
     */
    public void deleteRequest(String url) {
        StringRequest request = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(Users.this);
        rQueue.add(request);
    }

    public void doOnSuccess(String s){
        try {
            JSONObject obj = new JSONObject(s);

            if (obj!=null){
                Iterator i = obj.keys();
                String key = "";

                while(i.hasNext()){
                    key = i.next().toString();
                    if(!key.equals(UserDetails.username)) {
                        al.add(key);
                        references.add(new Firebase("https://qrcodechat-ca31a.firebaseio.com/messages/" + UserDetails.username + "_" + key)); //Aggiungo un riferimento per questo utente
                    }

                    totalUsers++;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(totalUsers <1){
            noUsersText.setVisibility(View.VISIBLE);
            usersList.setVisibility(View.GONE);
        }
        else{
            noUsersText.setVisibility(View.GONE);
            usersList.setVisibility(View.VISIBLE);
            usersList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, al));
        }

        for (int i = 0; i != al.size(); ++i) {
            references.get(i).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Map map = dataSnapshot.getValue(Map.class);
                    String message = map.get("message").toString();
                    String userName = map.get("user").toString();
                    String flag = map.get("flag").toString();
                    Utils u2 = new Utils();
                    if (u2.isExternalStorageWritable()) {

                        File fileToRead = new File(Environment.getExternalStorageDirectory() + File.separator + PRIVATE_KEY_FILENAME + "_"+ UserDetails.username + PRIVATE_KEY_EXTENSION);

                        try{
                            PrivateKey privKeyFromDevice = Utils.readPrivateKey(fileToRead);
                            byte[] toDecrypt = Hex.decodeHex(message.toCharArray());
                            String messageDecrypted = ae.decryptAsymmetric(toDecrypt, privKeyFromDevice);
                            if((!userName.equals(UserDetails.username))&&(flag.equals("1"))){
                                UserDetails.chatWith  = userName;

                                Intent notificationIntent = new Intent(getApplicationContext(), Chat.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                //Build the notification
                                Notification.Builder builder = new Notification.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.icon)
                                        .setContentIntent(contentIntent)
                                        .setContentTitle("Notifications from " + UserDetails.chatWith)
                                        .setAutoCancel(true)
                                        .setContentText(messageDecrypted);

                                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); //Creo un gestore della notifica
                                manager.notify(0, builder.build());
                            }
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }


                    } else {
                        Toast.makeText(Users.this, "External storage not available", Toast.LENGTH_LONG).show();
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
        pd.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_new_user:
                //add a new user, so I reset previous keys
                Constants.hisKey = "";
                startActivity(new Intent(Users.this, Actions.class));
                return true;
            case R.id.logout:
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

