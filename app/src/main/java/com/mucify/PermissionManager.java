package com.mucify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class PermissionManager {

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private static ActivityResultLauncher<String> mRequestPermissionLauncher = null;


    public static boolean requestPermission(AppCompatActivity activity, String permission) throws InterruptedException {
        // MY_TODO: Wait until dialog finished and return result

        if(mRequestPermissionLauncher == null) {
            mRequestPermissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(!isGranted)
                    throw new UnsupportedOperationException("Needs to grant permission");
            });
        }

        switch(permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if(ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED)
                    mRequestPermissionLauncher.launch(permission);
                break;
            case Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }

        return true;
    }
}
