package br.com.darksite.jazze.application;


import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class OfflineFeatures extends Application {

    private DatabaseReference utilisateurReference;
    private FirebaseAuth mAuth;
    private FirebaseUser utilisateurActuel;

    @Override
    public void onCreate() {
        super.onCreate();

        //https://www.improvein.com/blog/128-firebase-database-and-android-sync
        //https://firebase.google.com/docs/database/android/offline-capabilities
        //https://stackoverflow.com/questions/37448186/setpersistenceenabledtrue-crashes-app
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //https://stackoverflow.com/questions/23391523/load-images-from-disk-cache-with-picasso-if-offline
        //https://github.com/square/picasso/issues/1145
        //https://github.com/square/picasso/issues/862
        //https://newfivefour.com/android-image-caching-picasso.html
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso picasso = builder.build();
        picasso.setIndicatorsEnabled(true);
        picasso.setLoggingEnabled(true);
        Picasso.setSingletonInstance(picasso);

        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        utilisateurActuel = mAuth.getCurrentUser();

        if(utilisateurActuel != null){
            String online_User_ID = mAuth.getCurrentUser().getUid();

            utilisateurReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs").child(online_User_ID);
            utilisateurReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    utilisateurReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    //utilisateurReference.child("online").setValue(true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
