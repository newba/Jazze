package br.com.darksite.jazze.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissions {

    public static boolean validaPermissoes (int requestCode, Activity activity, String [] permissoes){

        if (Build.VERSION.SDK_INT >= 23) {

            List<String> listaPermissoes = new ArrayList<String>();


            //percorrer as permissoes que foram passadas e verificar se cada uma delas já tem permissao liberada

            for(String permissao : permissoes){
                Boolean validaPermissao =  ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;
                if(!validaPermissao) listaPermissoes.add(permissao);
            }

            //Caso a lista estiver vazia , nao é necessario pedir permissoes

            if(listaPermissoes.isEmpty()) return true;

            //Solicita permissao

            //É preciso converter a List em um Array de String para nao dar erro

            String[] permition = new String[ listaPermissoes.size()];
            listaPermissoes.toArray(permition);

            ActivityCompat.requestPermissions(activity, permition, requestCode);
        }

        return true;

    }
}
