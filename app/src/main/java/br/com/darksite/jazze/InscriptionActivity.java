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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;

import br.com.darksite.jazze.application.ConfigurationFirebase;


public class InscriptionActivity extends AppCompatActivity {

    private Toolbar inscriptionToolbar;
    private EditText edtNomInscription, edtCourrielInscription, edtMotDePasseInscription;
    private Button btnInscription;
    private FirebaseAuth autentication;
    private DatabaseReference stockerDonneesUtilisateur;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        autentication = ConfigurationFirebase.getFirebasejazzeAuth();


        inscriptionToolbar = (Toolbar)findViewById(R.id.inscrition_toolbar);

        setSupportActionBar(inscriptionToolbar);
      //  assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Inscription");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtNomInscription = (EditText) findViewById(R.id.edtInscriptionNom);
        edtCourrielInscription = (EditText) findViewById(R.id.edtInscriptionEmail);
        edtMotDePasseInscription = (EditText) findViewById(R.id.edtInscriptionMotDePasse);
        btnInscription = (Button) findViewById(R.id.btnInscription);
        loadingBar = new ProgressDialog(this);

        btnInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = edtNomInscription.getText().toString();
                final String email = edtCourrielInscription.getText().toString();
                final String pass = edtMotDePasseInscription.getText().toString();

                inscriptionUtilisateur(name, email, pass);
            }
        });

    }

    private void inscriptionUtilisateur(final String name, final String email, final String pass) {

        //Validations
        if (TextUtils.isEmpty(name)){
            Toast.makeText(InscriptionActivity.this, "Ce champ ne peut pas être vide", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(email)){
            Toast.makeText(InscriptionActivity.this, "Entrez votre courriel", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(pass)){
            Toast.makeText(InscriptionActivity.this, "Un mot de passe est requis", Toast.LENGTH_LONG).show();
        }

        else{

            loadingBar.setTitle("Enregistrement de compte");
            loadingBar.setMessage("Un moment s'il vous plaît. Nous enregistrons votre nouveau compte");
            loadingBar.show();

            autentication.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        Toast.makeText(InscriptionActivity.this, "Utilisateur enregistré", Toast.LENGTH_LONG).show();
                        //pegar o usuario autenticado e o seu id unico do Firebase Auth

                        String utilisateurActuel = autentication.getCurrentUser().getUid();

                        //fazer referencia ao banco de dados que criamos no firebase e criar o nó de usuarios
                        stockerDonneesUtilisateur = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs").child(utilisateurActuel);
                        stockerDonneesUtilisateur.child("nom_Utilisateur").setValue(name);

                        //stockerDonneesUtilisateur.child("user_Email").setValue(email);
                        //stockerDonneesUtilisateur.child("user_Pass").setValue(pass);

                        stockerDonneesUtilisateur.child("user_Status").setValue("Salut! J'utilise Jazze, développé par Marcelo Santos pour le Collége de Maisonneuve");
                        stockerDonneesUtilisateur.child("user_Image").setValue("userdefault");
                        stockerDonneesUtilisateur.child("device_Token").setValue(deviceToken);
                        stockerDonneesUtilisateur.child("user_Thumb").setValue("userdefaultImage").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){

                                    Intent i = new Intent (InscriptionActivity.this, MainActivity.class); //https://developer.android.com/reference/android/content/Intent.html
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();

                                }

                            }
                        });

                    }
                    else{
                        Toast.makeText(InscriptionActivity.this, "Erreur lors de l'inscription. Réessayer", Toast.LENGTH_LONG).show();
                    }

                    loadingBar.dismiss();

                }
            });

        }


    }
}
