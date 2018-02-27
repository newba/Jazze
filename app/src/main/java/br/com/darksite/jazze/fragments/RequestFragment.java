package br.com.darksite.jazze.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import br.com.darksite.jazze.R;
import br.com.darksite.jazze.application.ConfigurationFirebase;
import br.com.darksite.jazze.model.Requests;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView mRequeteListe;
    private View mRequeteView;
    private DatabaseReference contactRequestReference, utilisateurReference, getTypeReference, contactReference, contactInvitationReference;
    private FirebaseAuth mAuth;
    String utilisateurEnLigneID;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRequeteView = inflater.inflate(R.layout.fragment_request, container, false);

        mRequeteListe = (RecyclerView) mRequeteView.findViewById(R.id.request_liste);

        //Reference à l'utilisateur connecté
        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        utilisateurEnLigneID = mAuth.getCurrentUser().getUid();


        //Reference à la base de données (noued Invitations) + ID de l'utilisateur connecté
        contactRequestReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Invitations").child(utilisateurEnLigneID);
        //Reference à la base de données (noued Utilisateur)
        utilisateurReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs");
        //Reference à la base de données (noued Contacts)
        contactReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Contacts");
        // Reference à la base de données (noued Invitations)
        contactInvitationReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Invitations");




        mRequeteListe.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRequeteListe.setLayoutManager(linearLayoutManager);

        return mRequeteView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(
                Requests.class, // la classe de modèle
                R.layout.contact_request_all_users_laytout, // layout personalisée
                RequestFragment.RequestViewHolder.class, // Fragment
                contactRequestReference//reference à la base de données
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Requests model, int position) {
                final String listeIDUtilisateurs = getRef(position).getKey();
                getTypeReference = getRef(position).child("request_type").getRef();

                //Checker la valeur specifique dans la base des données et recuperer d'accord le type
                getTypeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String typeRequete = dataSnapshot.getValue().toString();

                            if (typeRequete.equals("reçu")) {

                                utilisateurReference.child(listeIDUtilisateurs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String userName = dataSnapshot.child("nom_Utilisateur").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_Thumb").getValue().toString();
                                        final String userStatus = dataSnapshot.child("user_Status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setThumbImage(thumbImage, getContext());
                                        viewHolder.setUserStatus(userStatus);

                                        //Creer une dialogBox
                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options[] = new CharSequence[]{
                                                        "Accepter demande",
                                                        "Annuler demande"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Sélectionner :");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int position) {
                                                        if(position == 0){

                                                            Calendar callForDate = Calendar.getInstance();
                                                            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
                                                            final String saveCurrentDate = currentDate.format(callForDate.getTime());

                                                            contactReference.child(utilisateurEnLigneID).child(listeIDUtilisateurs)
                                                                    .child("date")
                                                                    .setValue(saveCurrentDate)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            contactReference.child(listeIDUtilisateurs).child(utilisateurEnLigneID)
                                                                                    .child("date")
                                                                                    .setValue(saveCurrentDate)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {

                                                                                            contactInvitationReference.child(utilisateurEnLigneID).child(listeIDUtilisateurs)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                contactInvitationReference.child(listeIDUtilisateurs).child(utilisateurEnLigneID)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                    Toast.makeText(getContext(), "Requete accepté", Toast.LENGTH_SHORT).show();
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

                                                        if(position == 1){

                                                            contactInvitationReference.child(utilisateurEnLigneID).child(listeIDUtilisateurs)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                contactInvitationReference.child(listeIDUtilisateurs).child(utilisateurEnLigneID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    Toast.makeText(getContext(), "Demande annulée", Toast.LENGTH_SHORT).show();
                                                                                                }

                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                    }
                                                });
                                                builder.show();

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else if (typeRequete.equals("envoyé")) {

                                Button btnRequestSend = viewHolder.mView.findViewById(R.id.btn_request_ok);
                                btnRequestSend.setText("Requete envoyé");
                                viewHolder.mView.findViewById(R.id.btn_request_cancel).setVisibility(View.INVISIBLE);


                                utilisateurReference.child(listeIDUtilisateurs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String userName = dataSnapshot.child("nom_Utilisateur").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_Thumb").getValue().toString();
                                        final String userStatus = dataSnapshot.child("user_Status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setThumbImage(thumbImage, getContext());
                                        viewHolder.setUserStatus(userStatus);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options[] = new CharSequence[]{
                                                        "Annuler demande"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Requete envoyé :");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int position) {


                                                        if(position == 0){

                                                            contactInvitationReference.child(utilisateurEnLigneID).child(listeIDUtilisateurs)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                contactInvitationReference.child(listeIDUtilisateurs).child(utilisateurEnLigneID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    Toast.makeText(getContext(), "Demande annulée avec Succès", Toast.LENGTH_SHORT).show();
                                                                                                }

                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                                builder.show();

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRequeteListe.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }


        public void setUserName(String userName) {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.request_user_name);
            userNameDisplay.setText(userName);
        }

        public void setThumbImage(final String thumbImage, final Context ctx) {

            final CircleImageView thumb_Image = (CircleImageView) mView.findViewById(R.id.request_profile_image);
            //
            Picasso.with(ctx).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.userdefault).into(thumb_Image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.userdefault).into(thumb_Image);
                }
            });

        }

        public void setUserStatus(String userStatus) {
            TextView userStatusDisplay = (TextView) mView.findViewById(R.id.request_user_status);
            userStatusDisplay.setText(userStatus);
        }
    }

}
