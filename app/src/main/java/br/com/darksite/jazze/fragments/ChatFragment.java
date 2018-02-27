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
import br.com.darksite.jazze.model.Chats;
import br.com.darksite.jazze.model.Contacts;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private View mChatView;
    private RecyclerView mChatsList;
    private DatabaseReference contactReference, utilisateursReference;
    private FirebaseAuth mAuth;
    String utilisateurEnLigne;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mChatView = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatsList = (RecyclerView)mChatView.findViewById(R.id.chat_list);
        // prendre l'Uid d'utilisateur connecté
        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        utilisateurEnLigne = mAuth.getCurrentUser().getUid();

        contactReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Contacts").child(utilisateurEnLigne);
        //contactReference.keepSynced(true);
        utilisateursReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs");
       // utilisateursReference.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mChatsList.setLayoutManager(linearLayoutManager);

        return mChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats,ChatFragment.ChatsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(
                Chats.class,
                R.layout.liste_utilisateurs_layout,
                ChatFragment.ChatsViewHolder.class,
                contactReference) {

            @Override
            protected void populateViewHolder(final ChatFragment.ChatsViewHolder viewHolder, Chats model, int position) {

                final String listContactId = getRef(position).getKey();
                utilisateursReference.child(listContactId).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("nom_Utilisateur").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_Thumb").getValue().toString();

                        String userStatus = dataSnapshot.child("user_Status").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String online_Status = (String)dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_Status);
                        }

                        viewHolder.setUserName(userName);
                        viewHolder.setThumbImage(thumbImage, getContext());

                        viewHolder.setUserStatus(userStatus);


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (dataSnapshot.child("online").exists()){
                                    Intent chatIntent = new Intent (getContext(), ChatActivity.class);
                                    chatIntent.putExtra("destinataireUtilisateurUid", listContactId);
                                    chatIntent.putExtra("nom_Utilisateur", userName);
                                    startActivity(chatIntent);
                                }
                                else {
                                                
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
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mChatsList.setAdapter(firebaseRecyclerAdapter);


    }




    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
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

        public void setUserStatus(String userStatus) {
            TextView user_status = (TextView) mView.findViewById(R.id.listeUtilisateursStatus);
            user_status.setText(userStatus);
        }
    }
}
