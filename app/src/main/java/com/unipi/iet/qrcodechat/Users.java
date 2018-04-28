package com.unipi.iet.qrcodechat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Users extends AppCompatActivity {
    ListView usersList;
    TextView noUsersText;
    ArrayList<String> al = new ArrayList<>();
    int totalUsers = 0;
    ProgressDialog pd;
    //Lista di riferimenti al database
    ArrayList<Firebase> references = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        usersList = (ListView)findViewById(R.id.usersList);
        noUsersText = (TextView)findViewById(R.id.noUsersText);
        pd = new ProgressDialog(Users.this);
        pd.setMessage("Loading...");
        pd.show();

        String url = "https://qrcodechat-ca31a.firebaseio.com/exchanges/"+UserDetails.username+".json";

        StringRequest request = new StringRequest(Request.Method.GET, url, s -> doOnSuccess(s), volleyError -> System.out.println("" + volleyError));

        RequestQueue rQueue = Volley.newRequestQueue(Users.this);
        rQueue.add(request);

        //Ciclo for che attiva le notifiche
        for(int i = 0; i != references.size(); ++i) { //Per ogni riferimento
            String temp = al.get(i);
            references.get(i).addChildEventListener(new ChildEventListener() { //Si vede se vengono aggiunti nuovi nodi
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) { //Quando un nodo viene aggiunto
                    Map map = dataSnapshot.getValue(Map.class);
                    String message = map.get("message").toString(); //Prendiamo messaggio e username
                    String userName = map.get("user").toString();

                    if (!userName.equals(UserDetails.username)) { //Se l'username non è uguale al mio
                        UserDetails.chatWith = temp;                //Dico che voglio parlare con questo username
                        Intent notificationIntent = new Intent(getApplicationContext(), Chat.class); //Imposto un intent per aprire la chat con questo utente
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        Notification.Builder builder = new Notification.Builder(getApplicationContext()) //Costruisco la notifica
                                .setSmallIcon(R.drawable.icon)
                                .setContentIntent(contentIntent)
                                .setContentTitle("Notifications from " + UserDetails.chatWith)
                                .setContentText(message);
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); //Creo un gestore della notifica
                        manager.notify(0, builder.build());
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
        usersList.setOnItemClickListener((parent, view, position, id) -> {
            UserDetails.chatWith = al.get(position);
            Log.i("chatWith:",UserDetails.chatWith);
            startActivity(new Intent(Users.this, Chat.class));
        });
    }

    public void doOnSuccess(String s){
        try {
            JSONObject obj = new JSONObject(s);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
