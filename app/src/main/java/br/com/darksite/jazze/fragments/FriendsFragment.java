package br.com.darksite.jazze.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import br.com.darksite.jazze.ChatActivity;
import br.com.darksite.jazze.ProfilActivity;
import br.com.darksite.jazze.R;
import br.com.darksite.jazze.application.ConfigurationFirebase;
import br.com.darksite.jazze.model.Contacts;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mContactList;
    private DatabaseReference contactReference, utilisateursReference;
    private FirebaseAuth mAuth;
    private View mContactView;
    String utilisateurEnLigne;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContactView = inflater.inflate(R.layout.fragment_friends, container, false);

        mContactList = (RecyclerView)mContactView.findViewById(R.id.contactList);
        // prendre l'Uid d'utilisateur connecté
        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        utilisateurEnLigne = mAuth.getCurrentUser().getUid();

        contactReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Contacts").child(utilisateurEnLigne);
        contactReference.keepSynced(true);
        utilisateursReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs");
        utilisateursReference.keepSynced(true);
        mContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mContactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(
                        Contacts.class,
                        R.layout.liste_utilisateurs_layout,
                        ContactsViewHolder.class,
                        contactReference) {
            @Override
            protected void populateViewHolder(final ContactsViewHolder viewHolder, Contacts model, int position) {
                viewHolder.setDate(model.getDate());
                final String listContactId = getRef(position).getKey();
                utilisateursReference.child(listContactId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("nom_Utilisateur").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_Thumb").getValue().toString();
                        if(dataSnapshot.hasChild("online")){
                            String online_Status = (String)dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_Status);
                        }

                        viewHolder.setUserName(userName);
                        viewHolder.setThumbImage(thumbImage, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{
                                        userName + " - visiter le profil",
                                        "Envoyé un message"
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Sélectionner :");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {
                                        if(position == 0){
                                            Intent profileIntent = new Intent (getContext(), ProfilActivity.class);
                                            profileIntent.putExtra("destinataireUtilisateurUid", listContactId);
                                            startActivity(profileIntent);
                                        }

                                        if(position == 1){

                                            if (dataSnapshot.child("online").exists()){
                                                Intent chatIntent = new Intent (getContext(), ChatActivity.class);
                                                chatIntent.putExtra("destinataireUtilisateurUid", listContactId);
                                                chatIntent.putExtra("nom_Utilisateur", userName);
                                                startActivity(chatIntent);
                                            }
                                            else {
                                                /*
                                                https://www.programcreek.com/java-api-examples/index.php?api=com.firebase.client.ServerValue
                                                https://github.com/firebase/emberfire/issues/447
                                                https://stackoverflow.com/questions/36658833/firebase-servervalue-timestamp-in-java-data-models-objects
                                                 */
                                                utilisateursReference.child(listContactId).child("online")
                                                        .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Intent chatIntent = new Intent (getContext(), ChatActivity.class);
                                                        chatIntent.putExtra("destinataireUtilisateurUid", listContactId);
                                                        chatIntent.putExtra("nom_Utilisateur", userName);
                                                        startActivity(chatIntent);
                                                    }
                                                });

                                            }

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
        };
        mContactList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ContactsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDate(String date) {
            TextView devenusAmis = (TextView) mView.findViewById(R.id.listeUtilisateursStatus);
            devenusAmis.setText("Devenu Amis: "+ date);
        }

        public void setUserName(String userName){
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.listeUtilisateursNom);
            userNameDisplay.setText(userName);
        }

        public void setThumbImage(final String thumbImage, final Context ctx) {

            final CircleImageView thumb_Image = (CircleImageView) mView.findViewById(R.id.listeUtilisateursImageProfil);
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

        public void setUserOnline(String online_status) {
            ImageView onlineStatusView = (ImageView) mView.findViewById(R.id.online_status);
            if (online_status.equals("true")){
                onlineStatusView.setVisibility(View.VISIBLE);
            }
            else{
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
