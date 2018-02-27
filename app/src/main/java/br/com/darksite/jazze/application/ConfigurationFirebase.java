package br.com.darksite.jazze.application;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfigurationFirebase {

    //Singleton

    private static DatabaseReference referenceBaseDonnees;
    private static FirebaseAuth jazzeAuth;
    private static StorageReference jazzeStockage;

    public static DatabaseReference getFirebasejazzeDataBase(){
        if (referenceBaseDonnees == null){
            referenceBaseDonnees = FirebaseDatabase.getInstance().getReference();
        }

        return referenceBaseDonnees;
    }

    public static FirebaseAuth getFirebasejazzeAuth(){
        if(jazzeAuth == null){
            jazzeAuth = FirebaseAuth.getInstance();
        }
        return jazzeAuth;
    }

    public static StorageReference getStorageJazzeReference(){
        if (jazzeStockage == null){
            jazzeStockage = FirebaseStorage.getInstance().getReference();
        }
        return  jazzeStockage;
    }
}
