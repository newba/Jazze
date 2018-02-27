package br.com.darksite.jazze;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DepartActivity extends AppCompatActivity {

    private Button nouveauCompte, login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_depart);

        nouveauCompte = (Button) findViewById(R.id.btnToInscription);
        login = (Button) findViewById(R.id.btnToLogin);

        nouveauCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent nouveauCompteIntent = new Intent(DepartActivity.this, InscriptionActivity.class); //https://developer.android.com/reference/android/content/Intent.html
                startActivity(nouveauCompteIntent);

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loginIntent = new Intent(DepartActivity.this, LoginActivity.class);
                startActivity(loginIntent);

            }
        });
    }
}
