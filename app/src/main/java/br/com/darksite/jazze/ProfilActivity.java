package br.com.darksite.jazze;

import android.icu.util.Calendar;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import br.com.darksite.jazze.application.ConfigurationFirebase;


public class ProfilActivity extends AppCompatActivity {

    private Button sendContactRequestButton, refuserContactRequestButton;
    private TextView nomProfil, statusProfil;
    private ImageView imageProfil;
    private DatabaseReference utilisateursReference, contactInvitationReference, contactReference, notificationsReference;
    private FirebaseAuth mAuth;
    private String ETAT_ACTUEL;

    String destinataireUtilisateurUid, expediteurUtilisateurUid;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        // Creer un noued "Invitations" dans la base de donnees pour gérer les demandes entre les utilisateurs
        contactInvitationReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Invitations");
        contactInvitationReference.keepSynced(true);
        // prendre l'Uid d'utilisateur connecté
        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        expediteurUtilisateurUid = mAuth.getCurrentUser().getUid();

        // Creer un noued "Contacts" dans la base de donnees pour gérer les demandes entre les utilisateurs
        contactReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Contacts");
        contactReference.keepSynced(true);

        // Creer un noued "Notifications" dans la base de donnees pour gérer les demandes entre les utilisateurs
        notificationsReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Notifications");
        notificationsReference.keepSynced(true);


        utilisateursReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs");

        //prendre l'Uid d'utilisateur cliquée dans la list
        destinataireUtilisateurUid = getIntent().getExtras().get("destinataireUtilisateurUid").toString();


        sendContactRequestButton = (Button) findViewById(R.id.btnEnvoyerInvitation);
        refuserContactRequestButton = (Button) findViewById(R.id.btnRefuserInvitationContatc);
        nomProfil = (TextView) findViewById(R.id.txtNomUtilisateurProfilUnique);
        statusProfil = (TextView) findViewById(R.id.txtStatusUtilisateurProfilUnique);
        imageProfil = (ImageView) findViewById(R.id.imageProfilUniqueUtilisateur);
        ETAT_ACTUEL = "notAContact";

        //récupérer les données
        utilisateursReference.child(destinataireUtilisateurUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("nom_Utilisateur").getValue().toString();
                String status = dataSnapshot.child("user_Status").getValue().toString();
                String image = dataSnapshot.child("user_Image").getValue().toString();

                nomProfil.setText(name);
                statusProfil.setText(status);
                Picasso.with(ProfilActivity.this).load(image).placeholder(R.drawable.userdefault).into(imageProfil);

                //Prende les donnés du noued Invitations pour regler le bug de changement du button
                //https://firebase.google.com/docs/database/admin/retrieve-data
                //https://stackoverflow.com/questions/37703218/firebase-android-addlistenerforsinglevalueevent-sometimes-not-returning-data
                //https://www.programcreek.com/java-api-examples/index.php?api=com.firebase.client.ValueEventListener
                contactInvitationReference.child(expediteurUtilisateurUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild(destinataireUtilisateurUid)) {
                                    String typeRequete = dataSnapshot.child(destinataireUtilisateurUid)
                                            .child("request_type")
                                            .getValue()
                                            .toString();

                                    if (typeRequete.equals("envoyé")) {
                                        ETAT_ACTUEL = "requeteEnvoyée";
                                        sendContactRequestButton.setText("Annuler l'invitation");
                                        sendContactRequestButton.setBackgroundColor(0xFFFF0000);
                                        refuserContactRequestButton.setVisibility(View.INVISIBLE);
                                        refuserContactRequestButton.setEnabled(false);
                                    } else if (typeRequete.equals("reçu")) {
                                        ETAT_ACTUEL = "requeteReçu";
                                        sendContactRequestButton.setText("Accepter contact");
                                        sendContactRequestButton.setBackgroundColor(0xFF145619);

                                        refuserContactRequestButton.setVisibility(View.VISIBLE);
                                        refuserContactRequestButton.setEnabled(true);

                                        refuserContactRequestButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                refuserContactRequest();
                                            }
                                        });
                                    }
                                } else {
                                    contactReference.child(expediteurUtilisateurUid)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild(destinataireUtilisateurUid)) {
                                                        ETAT_ACTUEL = "yesAContact";
                                                        sendContactRequestButton.setText("Supprimer cette personne de mes contacts");
                                                        sendContactRequestButton.setBackgroundColor(0xFF6011DE);

                                                        refuserContactRequestButton.setVisibility(View.INVISIBLE);
                                                        refuserContactRequestButton.setEnabled(false);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Refuser Invitation de contact
        refuserContactRequestButton.setVisibility(View.INVISIBLE);
        refuserContactRequestButton.setEnabled(false);

        if (!expediteurUtilisateurUid.equals(destinataireUtilisateurUid)) {

            sendContactRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendContactRequestButton.setEnabled(false);
                    //verifier si les personnes sont ou non déjà contact
                    if (ETAT_ACTUEL.equals("notAContact")) {
                        sendContactRequest();
                    }
                    if (ETAT_ACTUEL.equals("requeteEnvoyée")) {
                        annulerContactRequest();
                    }

                    if (ETAT_ACTUEL.equals("requeteReçu")) {
                        accepterContactRequest();
                    }

                    if (ETAT_ACTUEL.equals("yesAContact")) {
                        suprimmerContact();
                    }
                }
            });

        } else {
            sendContactRequestButton.setVisibility(View.INVISIBLE);
            refuserContactRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void refuserContactRequest() {
        contactInvitationReference.child(expediteurUtilisateurUid).child(destinataireUtilisateurUid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactInvitationReference.child(destinataireUtilisateurUid).child(expediteurUtilisateurUid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendContactRequestButton.setEnabled(true);
                                                ETAT_ACTUEL = "notAContact";
                                                sendContactRequestButton.setText("ENVOYER UNE INVITATION DE CONTACT");
                                                sendContactRequestButton.setBackgroundColor(0xFF145619);
                                                refuserContactRequestButton.setVisibility(View.INVISIBLE);
                                                refuserContactRequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void suprimmerContact() {
        contactReference.child(expediteurUtilisateurUid).child(destinataireUtilisateurUid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            contactReference.child(destinataireUtilisateurUid).child(expediteurUtilisateurUid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                sendContactRequestButton.setEnabled(true);
                                                ETAT_ACTUEL = "notAContact";
                                                sendContactRequestButton.setText("ENVOYER UNE INVITATION DE CONTACT");
                                                sendContactRequestButton.setBackgroundColor(0xFF145619);
                                                refuserContactRequestButton.setVisibility(View.INVISIBLE);
                                                refuserContactRequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void accepterContactRequest() {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        final String saveCurrentDate = currentDate.format(callForDate.getTime());

        contactReference.child(expediteurUtilisateurUid).child(destinataireUtilisateurUid)
                .child("date")
                .setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        contactReference.child(destinataireUtilisateurUid).child(expediteurUtilisateurUid)
                                .child("date")
                                .setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        contactInvitationReference.child(expediteurUtilisateurUid).child(destinataireUtilisateurUid)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            contactInvitationReference.child(destinataireUtilisateurUid).child(expediteurUtilisateurUid)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                sendContactRequestButton.setEnabled(true);
                                                                                ETAT_ACTUEL = "yesAContact";
                                                                                sendContactRequestButton.setText("Supprimer cette personne de mes contacts");
                                                                                sendContactRequestButton.setBackgroundColor(0xFF6011DE);

                                                                                refuserContactRequestButton.setVisibility(View.INVISIBLE);
                                                                                refuserContactRequestButton.setEnabled(false);
                                                                            }

                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                    }
                                });

                    }
                });


    }

    private void annulerContactRequest() {
        contactInvitationReference.child(expediteurUtilisateurUid).child(destinataireUtilisateurUid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactInvitationReference.child(destinataireUtilisateurUid).child(expediteurUtilisateurUid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendContactRequestButton.setEnabled(true);
                                                ETAT_ACTUEL = "notAContact";
                                                sendContactRequestButton.setText("ENVOYER UNE INVITATION DE CONTACT");
                                                sendContactRequestButton.setBackgroundColor(0xFF145619);
                                                refuserContactRequestButton.setVisibility(View.INVISIBLE);
                                                refuserContactRequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void sendContactRequest() {
        contactInvitationReference.child(expediteurUtilisateurUid).child(destinataireUtilisateurUid)
                .child("request_type")
                .setValue("envoyé")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactInvitationReference.child(destinataireUtilisateurUid).child(expediteurUtilisateurUid)
                                    .child("request_type")
                                    .setValue("reçu")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                //http://javasampleapproach.com/android/firebase-realtime-database-get-list-of-data-example-android

                                                HashMap<String, String> notificationsData = new HashMap<>();
                                                notificationsData.put("from", expediteurUtilisateurUid);
                                                notificationsData.put("type", "requete");

                                                notificationsReference.child(destinataireUtilisateurUid).push().setValue(notificationsData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    sendContactRequestButton.setEnabled(true);
                                                                    ETAT_ACTUEL = "requeteEnvoyée";
                                                                    sendContactRequestButton.setText("Annuler l'invitation");
                                                                    sendContactRequestButton.setBackgroundColor(0xFFFF0000);
                                                                    refuserContactRequestButton.setVisibility(View.INVISIBLE);
                                                                    refuserContactRequestButton.setEnabled(false);
                                                                }
                                                            }
                                                        });


                                            }

                                        }
                                    });
                        }

                    }
                });
    }
}
