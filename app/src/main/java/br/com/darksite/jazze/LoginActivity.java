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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;

import br.com.darksite.jazze.application.ConfigurationFirebase;


public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button btnLogin;
    private EditText edtCourriel, edtMotDePasse;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference utilisateursReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        utilisateursReference = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs");

        mToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.login_toolbar);
        progressDialog = new ProgressDialog(this);

        setSupportActionBar(mToolbar);
        //  assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Connecter");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtCourriel = (EditText) findViewById(R.id.edtEmail);
        edtMotDePasse = (EditText) findViewById(R.id.edtMotDePasse);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = edtCourriel.getText().toString();
                String password = edtMotDePasse.getText().toString();

                validerConnection(email, password);

            }
        });


    }

    private void validerConnection(String email, String password) {

        //Validation
        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Entrez votre courriel", Toast.LENGTH_LONG).show();

        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this, "Un mot de passe est requis", Toast.LENGTH_LONG).show();

        }
        else{

            progressDialog.setTitle("Connexion");
            progressDialog.setMessage("Veuillez patienter pendant que nous vérifions vos informations d'identification");
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        String online_user_id = mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        utilisateursReference.child(online_user_id).child("device_Token").setValue(deviceToken)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(LoginActivity.this, "Vous êtes connecté!", Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(LoginActivity.this, MainActivity.class); // https://developer.android.com/reference/android/content/Intent.html
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        /*FLAG_ACTIVITY_NEW_TASK
                                        *Si elle est définie, cette activité deviendra le début d'une nouvelle tâche sur cette pile d'historique.
                                        *
                                        * FLAG_ACTIVITY_CLEAR_TASK
                                        * cet flag provoquera l'effacement de toute tâche existante associée à l'activité avant le démarrage de l'activité.
                                         */
                                        startActivity(i);
                                        finish();
                                    }
                                });
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Vérifiez votre email ou votre mot de passe", Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();

                }
            });

        }
    }
}
