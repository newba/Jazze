package br.com.darksite.jazze;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BienvenueActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenue);

        //Display la page de bienvenue pour 5 sec.
        // https://developer.android.com/guide/components/processes-and-threads.html
        // https://developer.android.com/training/basics/activity-lifecycle/recreating.html
        // http://www.decom.ufop.br/imobilis/tutorial-android-paralelismo-threads/

        Thread thread = new Thread(){

            @Override
            public void run() {

                try {

                    sleep(4000); // 4 sec...
                } catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    Intent i = new Intent(BienvenueActivity.this, MainActivity.class);
                    startActivity(i);
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
