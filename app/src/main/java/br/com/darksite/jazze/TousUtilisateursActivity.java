package br.com.darksite.jazze;


import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import br.com.darksite.jazze.application.ConfigurationFirebase;
import br.com.darksite.jazze.model.AllUsers;
import de.hdodenhof.circleimageview.CircleImageView;

public class TousUtilisateursActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView allUsersList;
    private DatabaseReference databaseListeUtilisateurReference;
    private EditText searchInputText;
    private ImageButton searchButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tous_utilisateurs);


        mToolbar = (Toolbar) findViewById(R.id.all_user_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Tous les utilisateurs");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchButton = (ImageButton)findViewById(R.id.btnSearchUser);
        searchInputText = (EditText)findViewById(R.id.search_input_text_user);

        allUsersList = (RecyclerView) findViewById(R.id.listeUtilisateursRV);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));

        databaseListeUtilisateurReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs");
        // offline
        databaseListeUtilisateurReference.keepSynced(true);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchUserName = searchInputText.getText().toString();
                if(TextUtils.isEmpty(searchUserName)){
                    Toast.makeText(TousUtilisateursActivity.this, "Entrez le nom d'un utilisateur", Toast.LENGTH_SHORT).show();
                }
                rechercerUtilisateurs(searchUserName);

            }
        });

    }

    private void rechercerUtilisateurs(String searchUserName ){
   // @Override
  //  protected void onStart() {

        Toast.makeText(TousUtilisateursActivity.this, "À la recherche...", Toast.LENGTH_SHORT).show();
        //FirebaseUI for Android — UI Bindings for Firebase
        //https://opensource.google.com/projects/firebaseui
        //https://github.com/firebase/FirebaseUI-Android
        //https://firebase.google.com/docs/reference/android/com/google/firebase/database/Query

        Query searchUsers = databaseListeUtilisateurReference.orderByChild("nom_Utilisateur")
                .startAt(searchUserName).endAt(searchUserName + "\uf8ff");

        //super.onStart();
        FirebaseRecyclerAdapter<AllUsers, AllUserViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUserViewHolder>
                (
                        AllUsers.class,
                        R.layout.liste_utilisateurs_layout,
                        AllUserViewHolder.class,
                        searchUsers
                        //databaseListeUtilisateurReference
                ) {
            @Override
            protected void populateViewHolder(AllUserViewHolder viewHolder, AllUsers model, final int position) {
                viewHolder.setNom_Utilisateur(model.getNom_Utilisateur());
                viewHolder.setUser_Status(model.getUser_Status());
                viewHolder.setUser_Thumb(getApplicationContext(), model.getUser_Thumb());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String destinataireUtilisateurUid = getRef(position).getKey();

                        Intent profilItent = new Intent(TousUtilisateursActivity.this, ProfilActivity.class );

                        profilItent.putExtra("destinataireUtilisateurUid", destinataireUtilisateurUid);

                        startActivity(profilItent);
                    }
                });

            }
        };

        allUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class AllUserViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public AllUserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setNom_Utilisateur (String nom_Utilisateur){
            TextView nom = (TextView) mView.findViewById(R.id.listeUtilisateursNom);
            nom.setText(nom_Utilisateur);
        }

        public void setUser_Status (String user_Status){
            TextView status = (TextView) mView.findViewById(R.id.listeUtilisateursStatus);
            status.setText(user_Status);
        }

        public void setUser_Thumb (final Context ctx, final String user_Thumb){
            final CircleImageView thumbImage = (CircleImageView) mView.findViewById(R.id.listeUtilisateursImageProfil);
            //
            Picasso.with(ctx).load(user_Thumb).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.userdefault).into(thumbImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(user_Thumb).placeholder(R.drawable.userdefault).into(thumbImage);
                }
            });




        }
    }

}
