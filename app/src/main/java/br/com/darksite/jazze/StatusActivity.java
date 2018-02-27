package br.com.darksite.jazze;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import br.com.darksite.jazze.application.ConfigurationFirebase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button btnchangerStatus;
    private EditText edtChangerStatus;
    private DatabaseReference changerStatus;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        String utilisateurUID = mAuth.getCurrentUser().getUid();
        changerStatus = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs").child(utilisateurUID);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Changer votre status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);
        btnchangerStatus = (Button) findViewById(R.id.btnSaveStatus);
        edtChangerStatus = (EditText) findViewById(R.id.edtStatusEdition);

        //Obtenir le statut précédent d'utilisateur
        String old_Status = getIntent().getExtras().get("user_Status").toString();
        edtChangerStatus.setText(old_Status);

        btnchangerStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nouveauStatut = edtChangerStatus.getText().toString();

                changementStatusUtilisateur(nouveauStatut);
            }
        });
    }

    public void  changementStatusUtilisateur(String nouveauStatut){

        if(TextUtils.isEmpty(nouveauStatut)){
            Toast.makeText(StatusActivity.this, "Veuillez entrer votre statut", Toast.LENGTH_SHORT).show();
        }

        else{

            loadingBar.setTitle("Changement de Status");
            loadingBar.setMessage("Mise à jour de votre statut S'il vous plaît, attendez");
            loadingBar.show();
            changerStatus.child("user_Status").setValue(nouveauStatut).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        loadingBar.dismiss();
                        Intent statusIntent = new Intent(StatusActivity.this, ParametresActivity.class);
                        startActivity(statusIntent);
                        Toast.makeText(StatusActivity.this, "Le statut a été changé", Toast.LENGTH_SHORT).show();


                    }

                    else {
                        Toast.makeText(StatusActivity.this, "Erreur lors de l'enregistrement. Réessayer", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

    }
}
