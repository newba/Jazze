package br.com.darksite.jazze;

import android.content.Context;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.darksite.jazze.adapteur.MessagesAdapter;
import br.com.darksite.jazze.application.ConfigurationFirebase;
import br.com.darksite.jazze.application.LastSeen;
import br.com.darksite.jazze.model.Messages;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageDestinataireID, messageDestinataireNom, messageExpediteurID;
    private Toolbar chatToolbar;
    private TextView userName, lastSeen;
    private CircleImageView userImageChatActivity;
    private DatabaseReference racineReference;
    private ImageButton btnEnvoyeImage, btnEnvoyerUnMessage;
    private EditText inputMessage;
    private FirebaseAuth mAuth;
    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        racineReference = ConfigurationFirebase.getFirebasejazzeDataBase();
        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        messageExpediteurID = mAuth.getCurrentUser().getUid();


        messageDestinataireID = getIntent().getExtras().get("destinataireUtilisateurUid").toString();
        messageDestinataireNom = getIntent().getExtras().get("nom_Utilisateur").toString();

        chatToolbar = (Toolbar)findViewById(R.id.chat_bar_layout);
        //Inserer l'action bar customisé dans l'activité de chat
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = layoutInflater.inflate(R.layout.chat_titre, null);

        actionBar.setCustomView(actionbarView);

        userName = (TextView)findViewById(R.id.txtDiplayUserNameChat);
        lastSeen = (TextView)findViewById(R.id.txtLastSeenChat);
        userImageChatActivity = (CircleImageView)findViewById(R.id.imageDisplayChat);
        btnEnvoyeImage = (ImageButton)findViewById(R.id.btnEnvoyeImage);
        btnEnvoyerUnMessage = (ImageButton)findViewById(R.id.btnEnvoyerUnMessage);
        inputMessage = (EditText) findViewById(R.id.txtEcrireUnMessage);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView)findViewById(R.id.messages_list_users);

        linearLayoutManager = new LinearLayoutManager(this);

        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);

        afficherMessages();

        userName.setText(messageDestinataireNom);

        racineReference.child("Utilisateurs").child(messageDestinataireID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String userThumb = dataSnapshot.child("user_Thumb").getValue().toString();

                Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.userdefault).into(userImageChatActivity, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ChatActivity.this).load(userThumb).placeholder(R.drawable.userdefault).into(userImageChatActivity);
                    }
                });
                if(online.equals("true")){
                    lastSeen.setText("Online");

                }
                else {
                    LastSeen getTime = new LastSeen();
                    long lastseen = Long.parseLong(online);

                    if(lastseen >= 0){
                        String lastseenDisplay = getTime.getTimeAgo(lastseen, getApplicationContext());
                        lastSeen.setText(lastseenDisplay);
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnEnvoyerUnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessages();
            }
        });


    }

    private void afficherMessages() {
        racineReference.child("Messages").child(messageExpediteurID).child(messageDestinataireID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged(); 
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
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessages() {

        String messageText = inputMessage.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this, "Vous devez écrire un message", Toast.LENGTH_SHORT).show();
        }
        else{
            //Creer reference pour les utilisateur que vont envoyer les messages
            //Expediteur
            String messageExpediteurReference = "Messages/" + messageExpediteurID + "/" + messageDestinataireID;
            //Destinataire
            String messageDestinataireReference = "Messages/" + messageDestinataireID + "/" + messageExpediteurID;

            // Creer une key + un noued "Messages" dans la base de donnees + les children`s
            DatabaseReference utilisateurMessageKey = racineReference.child("Messages").child(messageExpediteurID).child(messageDestinataireID).push();

            // Stocker le key
            String messagePushID = utilisateurMessageKey.getKey();

            //Pour écrire simultanément à des enfants spécifiques d'un nœud sans surcharger les autres nœuds enfants, utilisez la méthode updateChildren
            //https://firebase.google.com/docs/database/android/read-and-write#next_steps
            //https://stackoverflow.com/questions/36223373/firebase-updatechildren-vs-setvalue
            Map compositionMessage = new HashMap();

            compositionMessage.put("message", messageText);
            compositionMessage.put("vu", false);
            compositionMessage.put("type", "text");
            compositionMessage.put("horaire", ServerValue.TIMESTAMP);
            compositionMessage.put("from", messageExpediteurID);

            Map messageDetaille = new HashMap();

            messageDetaille.put(messageExpediteurReference + "/" + messagePushID, compositionMessage);
            messageDetaille.put(messageDestinataireReference + "/" + messagePushID, compositionMessage);

            racineReference.updateChildren(messageDetaille, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null){
                        Log.d("ChatLog : ", databaseError.getMessage().toString());
                    }
                    inputMessage.setText("");
                }
            });




        }
    }
}
