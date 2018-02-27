package br.com.darksite.jazze;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import br.com.darksite.jazze.adapteur.OngletsPageAdapter;
import br.com.darksite.jazze.application.ConfigurationFirebase;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    //https://developer.android.com/reference/android/support/v7/widget/Toolbar.html
    //https://stackoverflow.com/questions/29025961/setsupportactionbar-toolbar-error
    private FirebaseAuth mAuth;
    private DatabaseReference utilisateurReference;
    FirebaseUser utilisateurActuel;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private OngletsPageAdapter mOngletsPageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        utilisateurActuel = mAuth.getCurrentUser();

        if(utilisateurActuel != null) {
            String online_User_ID = mAuth.getCurrentUser().getUid();

            utilisateurReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs").child(online_User_ID);
        }



        mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Jazze");

        //Onglets
        mViewPager = (ViewPager) findViewById(R.id.main_ongles_pages);
        mOngletsPageAdapter = new OngletsPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mOngletsPageAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_onglets);

        mTabLayout.setupWithViewPager(mViewPager);

    }



    @Override
    protected void onStart() {
        super.onStart();

        utilisateurActuel = mAuth.getCurrentUser();

        if (utilisateurActuel == null){

            deconnexion();
        }
        else if(utilisateurActuel != null) {
            utilisateurReference.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(utilisateurActuel != null) {
            utilisateurReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void deconnexion() {

            Intent departIntent = new Intent(MainActivity.this, DepartActivity.class);
            departIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // ne permet pas à l'utilisateur de revenir à la main activity
            startActivity(departIntent);
            finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_deconnexion){
            if(utilisateurActuel != null){
                utilisateurReference.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mAuth.signOut();
            deconnexion();
        }

        if(item.getItemId() == R.id.main_settings){
            Intent parametresIntent = new Intent(MainActivity.this, ParametresActivity.class);
            startActivity(parametresIntent);
        }

        if(item.getItemId() == R.id.main_listeUtilisateurs){
            Intent listeUtilisateursIntent = new Intent(MainActivity.this, TousUtilisateursActivity.class);
            startActivity(listeUtilisateursIntent);
        }

        if(item.getItemId() == R.id.main_propos){
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.darksite.com.br"));

            startActivity(intent);
        }

        return  true;
    }
}
