package rcos.findme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Garret on 11/4/2015.
 */
public class LocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int ONE_MINUTE = 1000 * 60;

    private boolean gpsProviderActive;
    private boolean networkProviderActive;

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderApi fusedLocationProvider;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private LocationListener locationListener;
    private PermissionCheck permissionCheck;

    private Location lastLocation;
    private Location currentLocation;
    private String lastUpdateTime;

    private android.app.Activity activity;

    public LocationService(android.app.Activity act) {
        activity = act;
        permissionCheck = new PermissionCheck(activity);
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        buildGoogleApiClient();
        //buildLocationListener();
        createLocationRequest();

        if (!(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            showMessageOKCancel("Location Services not Active", "Please enable Location Services and GPS", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int x) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(intent);
                }
            });
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(activity.getBaseContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest()
                .setInterval(5000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            currentLocation = lastLocation;
        }

        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // disable ui components that require Google APIs until onConnected is called
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }
    /*
    todo: implement onSaveInstanceState... saves the current instance in the event the activity gets destroyed.

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                RequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, currentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, lastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    todo: implement updateValuesFromBundle

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // update data based on data saved in onSaveInstanceState
        }
    }
    */

    protected boolean betterLocation(Location location) {
        if (currentLocation == null) {
            return true;
        }

        long timeDifference = location.getTime() - currentLocation.getTime();
        boolean timeSignificantlyNewer = timeDifference > ONE_MINUTE;
        boolean timeSignificantlyOlder = timeDifference < -ONE_MINUTE;
        boolean newer = timeDifference > 0;

        if(timeSignificantlyNewer) {
            return true;
        } else if (timeSignificantlyOlder) {
            return false;
        }

        int accuracyDifference = (int) (location.getAccuracy() - currentLocation.getAccuracy());
        boolean moreAccurate = accuracyDifference < 0;
        boolean significantlyLessAccurate = accuracyDifference > 160;

        if (moreAccurate) {
            return true;
        } else if (newer && !significantlyLessAccurate) {
            return true;
        }
        return false;
    }

    private void showMessageOKCancel(String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(activity).setTitle(title).setMessage(message).setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null).create().show();
    }
}
