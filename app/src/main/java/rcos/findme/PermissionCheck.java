package rcos.findme;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Garret on 11/6/2015.
 */
public class PermissionCheck {
    private final int REQUEST_CODE_PERMISSION = 99;
    private android.app.Activity activity;

    public PermissionCheck(android.app.Activity act) {
        activity = act;
    }

    public boolean CheckLocationPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION))
                    showMessageOKCancel("Please allow access to Location for this application to work.", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int x) {
                            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
                        }
                    });
                else {
                    ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
                }
            }
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            return false;
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(activity).setMessage(message).setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null).create().show();
    }
}
