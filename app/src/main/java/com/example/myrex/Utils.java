package com.example.myrex;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.*;
import android.support.v4.app.ActivityCompat;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

public class Utils
{
    public static int REQUEST_ENABLE_BT = 1;
    public static int REQUEST_COARSE_LOCATION = 2;
    public static int REQUEST_FINE_LOCATION = 3;

    private static final String TAG = "rex_test_util";

    public static boolean checkLocationPermission(final Activity context)
    {
        Log.d(TAG,"checkLocationPermission");

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.d(TAG,"shouldShowRequestPermissionRationale");
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                Log.d(TAG,"requestPermissions");
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
}
