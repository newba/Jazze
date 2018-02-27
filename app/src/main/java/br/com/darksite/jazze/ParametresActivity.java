package br.com.darksite.jazze;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.com.darksite.jazze.application.ConfigurationFirebase;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ParametresActivity extends AppCompatActivity {

    private CircleImageView photoParametres;
    private TextView nomUtilisateur, statusUtilisateur;
    private Button btnChangerPhotoUtilisateur, btnChangerStatusUtilisateur;
    private DatabaseReference getDonneesUtilisateur;
    private FirebaseAuth mAuth;
    private StorageReference stockerImageProfil, thumbImageReference;
    private ProgressDialog loadingBar;


    private final static int Galerie_Photo = 1;

    Bitmap thumbBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        loadingBar = new ProgressDialog(this);
        photoParametres = (CircleImageView) findViewById(R.id.imgProfiilDefault);
        nomUtilisateur = (TextView) findViewById(R.id.txtNomProfil);
        statusUtilisateur = (TextView) findViewById(R.id.txtStatusUtilisateur);
        btnChangerPhotoUtilisateur = (Button) findViewById(R.id.btnChangePhotoProfil);
        btnChangerStatusUtilisateur = (Button) findViewById(R.id.btnChangeStatusProfil);

        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();
        //pegar o usuario autenticado e o seu id unico do Firebase Auth
        String utilisateurAuthentifieId = mAuth.getCurrentUser().getUid();

        //referencia no banco de dados ao usuario autenticado - aponta para o nó especifico e para o id unico especifico
        getDonneesUtilisateur = ConfigurationFirebase.getFirebasejazzeDataBase().child("Utilisateurs").child(utilisateurAuthentifieId);
        getDonneesUtilisateur.keepSynced(true);

        //referencia ao storage do firebase e criar uma pasta dentro do módulo
        stockerImageProfil = ConfigurationFirebase.getStorageJazzeReference().child("Images_Profil");
        thumbImageReference = ConfigurationFirebase.getStorageJazzeReference().child("Thumb_Images");

        getDonneesUtilisateur.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /*dataSnapshot: Un DataSnapshot contient des données provenant d'un emplacement de base de données.
                  Chaque fois que vous lisez des données de la base de données, vous recevez les données en tant que DataSnapshot

                  https://firebase.google.com/docs/reference/js/firebase.database.DataSnapshot
                  https://stackoverflow.com/questions/42950532/android-extracting-data-from-firebase-data-snapshot
                */

                String nom_utilisateur = dataSnapshot.child("nom_Utilisateur").getValue().toString();
                String user_Status = dataSnapshot.child("user_Status").getValue().toString();
                final String user_Image = dataSnapshot.child("user_Image").getValue().toString();
                String user_Thumb = dataSnapshot.child("user_Thumb").getValue().toString();

                nomUtilisateur.setText(nom_utilisateur);
                statusUtilisateur.setText(user_Status);

                //https://firebase.google.com/docs/storage/android/start?authuser=0
                //http://square.github.io/picasso/
                if (!user_Image.equals("userdefault")){


                    Picasso.with(ParametresActivity.this).load(user_Image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.userdefault).into(photoParametres, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ParametresActivity.this).load(user_Image)
                                   .placeholder(R.drawable.userdefault).into(photoParametres);

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnChangerPhotoUtilisateur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //https://stackoverflow.com/questions/17765265/difference-between-intent-action-get-content-and-intent-action-pick
                //https://www.youtube.com/watch?v=HDJP0yj04n8

                Intent galerieIntent  = new Intent();
                galerieIntent.setAction(Intent.ACTION_GET_CONTENT);
                galerieIntent.setType("image/*");
                startActivityForResult(galerieIntent, Galerie_Photo);
            }
        });

        btnChangerStatusUtilisateur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Prende le text existent dans le status pour l'apporter à le editText du Changement de Status
                String oldStatus = statusUtilisateur.getText().toString();

                Intent statusIntent = new Intent(ParametresActivity.this, StatusActivity.class);
                statusIntent.putExtra("user_Status", oldStatus);
                startActivity(statusIntent);
            }
        });


    }

    //https://developer.android.com/training/basics/intents/result.html
    //https://stackoverflow.com/questions/28450049/how-get-result-from-onactivityresult-in-fragment
    //https://stackoverflow.com/questions/920306/sending-data-back-to-the-main-activity-in-android/46297977
    //https://theartofdev.com/2015/02/15/android-cropping-image-from-camera-or-gallery/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Galerie_Photo && resultCode == RESULT_OK && data != null){

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Mise à jour de l'image de profil");
                loadingBar.setMessage("Veuillez patienter pendant la mise à jour de votre image de profil");
                loadingBar.show();

                Uri resultUri = result.getUri();

                //prendre l'image original et comprimer
                // https://github.com/zetbaitsu/Compressor
                File thumbPath = new File (resultUri.getPath());

                //stocker l'image dans firebase et le nommee avec l'id unique
                String uIDUtilisateur = mAuth.getCurrentUser().getUid();

                try{

                    thumbBitmap = new Compressor(this).setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(65)
                            .compressToBitmap(thumbPath);

                }
                catch (IOException e){
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 65, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();


                StorageReference filePath = stockerImageProfil.child(uIDUtilisateur + ".jpg");
                final StorageReference fileThumbPath = thumbImageReference.child(uIDUtilisateur + "_thumb"+ ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(ParametresActivity.this, "Nous téléchargeons l'image", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = fileThumbPath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                    if (thumb_task.isSuccessful()){
                                        Map updateDonneesUtilisateur = new HashMap();
                                        updateDonneesUtilisateur.put("user_Image", downloadUrl);
                                        updateDonneesUtilisateur.put("user_Thumb", thumb_downloadUrl);

                                        getDonneesUtilisateur.updateChildren(updateDonneesUtilisateur)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                Toast.makeText(ParametresActivity.this,"Image enregistrée avec succès", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        });
                                    }

                                }
                            });


                        }
                        else{
                            Toast.makeText(ParametresActivity.this, "Il y a un erreur", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
