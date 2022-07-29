package com.example.geolocation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_COARSE_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void OpenMap(View view) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d("Permission", "API < 23");
            GetGeolocation(); // Envía al usuario a MapActivity.
        } else { // API >= 23
            Log.d("Permission", "API >= 23");
            VerificarPermisos(); // Serie de condiciones antes de enviar al usuario a MapActivity
        }
        
    }

    private void GetGeolocation() {
        startActivity(new Intent(MainActivity.this, MapActivity.class));
    }

    private void VerificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permiso de aproximación y precision consedidos
            Log.d("Permission", "Permission granted");
            GetGeolocation();
        } else {
            Log.d("Permission", "Approach and Precision permit required");

            // verificar si el usuario anteriormente rechazó el permiso
            if ( (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) ||
                 (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))   ) {
                Log.d("Permission", "The user previously rejected the request.");
            } else {
                Log.d("Permission", "Request permission");
            }

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_PERMISSION_COARSE_LOCATION);
        }

    } // End verificar permiso

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_COARSE_LOCATION){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission", "Permission granted (request)");
                GetGeolocation();
            } else {
                Log.d("Permission", "Permission denied (request)");
                // Averiguar si anteriormente se rechazaron los permisos
                if ( (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) ||
                        (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))   ) {
//"You need to enable permission to use this App"
                    new AlertDialog.Builder(this).setMessage(getString(R.string.btn_main_try_again_message))
                            .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                                    }, REQUEST_PERMISSION_COARSE_LOCATION);
                                }
                            })
                            .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Leave
                                    Log.d("Permission", "User leave?");
                                }
                            }).show();
                } else {
                    Log.d("Permission", "You need to ablle permission manually");
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}