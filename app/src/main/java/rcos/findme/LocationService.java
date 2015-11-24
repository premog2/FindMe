package rcos.findme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Garret on 11/4/2015.
 */
public class LocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int ONE_MINUTE = 1000 * 60;
    private static final String LOC_SETTING_TITLE = "Location Services not active";
    private static final String LOC_SETTING_BODY = "Please enable Location Services and GPS for this application to function properly";

    private boolean gpsProviderActive;
    private boolean networkProviderActive;
    private boolean requestingLocationUpdates;
    private boolean resolvingError = false;

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderApi fusedLocationProvider;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private PermissionCheck permissionCheck;
    private LocationUpdateCallback locationUpdateCallback;

    private Location lastLocation;
    private Location currentLocation;
    private String lastUpdateTime;

    private android.app.Activity activity;

    public LocationService(android.app.Activity act, LocationUpdateCallback callback) {
        activity = act;
        locationUpdateCallback = callback;
        permissionCheck = new PermissionCheck(activity);
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        buildGoogleApiClient();

        if (permissionCheck.CheckLocationPermission()) {
            createLocationRequest();
            attemptConnection();
            attemptGpsConnection();
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        setRequestingLocationUpdates(true);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        setRequestingLocationUpdates(false);
    }

    // basic creation of location request
    // todo: have conditional location requests, to vary accuracy and increase performance
    protected void createLocationRequest() {
        requestingLocationUpdates = true;
        locationRequest = new LocationRequest()
                .setInterval(7000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void attemptConnection() {
        if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
            // will call onConnected once connected
            googleApiClient.connect();
        }
    }

    protected void disconnect() {
        stopLocationUpdates();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null && betterLocation(lastLocation)) {
            currentLocation = lastLocation;
        }
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // disable ui components that require Google APIs until onConnected is called
        attemptConnection();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (resolvingError) {
            return;
        } else if (result.hasResolution()) {
            try {
                resolvingError = true;
                result.startResolutionForResult(activity, ErrorCodes.REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // try again
                attemptConnection();
            }
        } else {
            resolvingError = true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (betterLocation(location)) {
            Log.i("LOCATION CHANGED", location.toString());
            currentLocation = location;
            lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            locationUpdateCallback.locationUpdated(location);
        }
    }

    private boolean betterLocation(Location location) {
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

    protected void setActivity(android.app.Activity act, LocationUpdateCallback callback) {
        activity = act;
        locationUpdateCallback = callback;
    }

    protected boolean isRequestingLocationUpdates() {
        return requestingLocationUpdates;
    }

    protected void setRequestingLocationUpdates(boolean value) {
        requestingLocationUpdates = value;
    }

    protected Location getCurrentLocation() {
        return currentLocation;
    }

    protected void setCurrentLocation(Location loc) {
        currentLocation = loc;
    }

    protected String getLastUpdateTime() {
        return lastUpdateTime;
    }

    protected void setLastUpdateTime(String updateTime) {
        lastUpdateTime = updateTime;
    }

    protected void setResolvingError(boolean value) {
        resolvingError = value;
    }

    protected boolean isGoogleApiClientConnecting() {
        return googleApiClient.isConnecting();
    }

    protected boolean isGoogleApiClientConnected() {
        return googleApiClient.isConnected();
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(activity.getBaseContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    protected synchronized void attemptGpsConnection() {
        if (!(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            showMessageOKCancel(LOC_SETTING_TITLE, LOC_SETTING_BODY, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int x) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(intent);
                }
            });
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsProviderActive = true;
        } else {
            gpsProviderActive = false;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            networkProviderActive = true;
        } else {
            networkProviderActive = false;
        }
    }

    private void showMessageOKCancel(String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
