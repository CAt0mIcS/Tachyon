package com.de.mucify;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class PermissionManager {

    /**
     * Register the permissions callback, which handles the user's response to the
     * system permissions dialog. Save the return value, an instance of
     * ActivityResultLauncher, as an instance variable.
     */
    private static ActivityResultLauncher<String> mRequestPermissionLauncher = null;

    private static boolean mPermissionGranted = false;
    private static volatile boolean mPermissionResultHere = false;


    public static boolean requestPermission(AppCompatActivity activity, String permission) {
        // MY_TODO: Already load certain things while user still accepts permission
        // MY_TODO: Dialog explaining why we need permission

        if (mRequestPermissionLauncher == null)
            mRequestPermissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                mPermissionGranted = result;
                mPermissionResultHere = true;
            });

        mPermissionGranted = false;
        mPermissionResultHere = false;

        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
            case Manifest.permission.READ_PHONE_STATE:
                if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                    mRequestPermissionLauncher.launch(permission);
                    return waitForPermissionResult();
                }
                break;
            case Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }

        return true;
    }

    private static boolean waitForPermissionResult() {
        while (!mPermissionResultHere) {
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mPermissionGranted;
    }
}