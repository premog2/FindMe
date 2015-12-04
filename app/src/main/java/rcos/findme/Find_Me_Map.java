package rcos.findme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class Find_Me_Map extends AppCompatActivity implements LocationUpdateCallback, SensorEventListener {

    public static final String TAG = Find_Me_Map.class.getSimpleName();

    private GoogleMap map;
    private LocationService locationService;
    private boolean halfway;

    private Location location;
    private LatLng latlng;
    private float accuracy;
    private Circle accCircle;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Sensor aSensor;
    private float heading;
    private final int SENSOR_DELAY = 100000000; // Time in microseconds between sensor updates

    private Location friendLocation;
    private LatLng friendLatLng;

    private Marker userMarker;
    private Marker friendMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        locationService = new LocationService(this, this);

        if(intent.hasExtra(IntentExtras.LOCATION)) {
            location = intent.getParcelableExtra(IntentExtras.LOCATION);
        }
        if (location != null) {
            latlng = new LatLng(location.getLatitude(), location.getLongitude());
            accuracy = location.getAccuracy();
        }

        friendLocation = intent.getParcelableExtra(IntentExtras.FRIEND_LOCATION);
        if (friendLocation != null) {
            friendLatLng = new LatLng(friendLocation.getLatitude(), friendLocation.getLongitude());
        }

        halfway = intent.getBooleanExtra(IntentExtras.HALFWAY, false);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_find_me_map);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (!mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.i("Sensor update", "Error registering magnetic field sensor");
        }
        if (!mSensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            Log.i("Sensor update", "Error registering acceleromoter sensor");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void locationUpdated(Location loc) {
        location = loc;
        latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
        userMarker.setPosition(latlng);
        if (accuracy != 0) {
            accCircle.setCenter(latlng);
            accCircle.setRadius(loc.getAccuracy());
        }
        Log.i("LocationUpdated", "changed marker position");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //unused
    }

    private float [] m;
    private float [] a;
    private int average = 0;
    private int sensorChanges = -1;
    private final int SENSOR_READINGS_PER_AVERAGE = 50;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            a = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD &&
                userMarker != null && a != null) {

            m = event.values.clone();
            float [] R = new float[9];
            float [] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, a, m)) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                heading = orientation[0] * 57.2958f;
                if (heading < 0) {
                    heading = 360 + heading;
                }

                average  += heading;
                if (sensorChanges == -1) {
                    sensorChanges = 1;
                    userMarker.setRotation(heading);
                } else if (sensorChanges < SENSOR_READINGS_PER_AVERAGE - 1) {
                    sensorChanges++;
                } else {
                    sensorChanges = 0;
                    average = average / SENSOR_READINGS_PER_AVERAGE;

                    userMarker.setRotation(average);
                    average = 0;
                    Log.i("Sensor update", "Updated heading and rotation to: " + heading);
                }

            }  else {
                Log.i("Sensor update", "Error getting rotation matrix");
            }
        }
        if (userMarker == null){
            Log.i("Sensor update", "Error: userMarker == null");
        }
        if (m == null){
            Log.i("Sensor update", "Error: m == null");
        }
        if (a == null){
            Log.i("Sensor update", "Error: a == null");
        }
        //Log.i("SensorEvent", "geomagnetic sensor changed to: " + event.values[0]);
    }

    private void addMarkersToMap() {
        //resize bitmaps
        Bitmap userBitmap;
        userBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.usermarker);

        userBitmap = Bitmap.createScaledBitmap(userBitmap,
                Math.round(userBitmap.getWidth()/1.8f),
                Math.round(userBitmap.getHeight()/1.8f), false);

        Bitmap friendBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.friendmarker);
        friendBitmap = Bitmap.createScaledBitmap(friendBitmap,
                Math.round(friendBitmap.getWidth()/4.3f),
                Math.round(friendBitmap.getHeight()/4.3f), false);

        //Clear map and add markers
        map.clear();
        userMarker = map.addMarker(new MarkerOptions()
                .position(latlng)
                .icon(BitmapDescriptorFactory.fromBitmap(userBitmap))
                .anchor(0.5f, 0.5f)
                .flat(true));
        Log.i("AddMarkers", "added user marker");
        friendMarker = map.addMarker(new MarkerOptions()
                .position(friendLatLng)
                .title("Friend's Position")
                .icon(BitmapDescriptorFactory.fromBitmap(friendBitmap)));
        Log.i("AddMarkers", "added friend marker");

        if (accuracy != 0) {
            //Add location accuracy circle
            CircleOptions accCircleOptions = new CircleOptions()
                    .center(latlng)
                    .radius(accuracy)
                    .strokeColor(Color.argb(100, 255, 255, 255))
                    .fillColor(Color.argb(75, 33, 150, 243));
            accCircle = map.addCircle(accCircleOptions);
        }

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        map.getUiSettings().setMapToolbarEnabled(false);
        addMarkersToMap();

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12));


        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, map.getMaxZoomLevel()), 2000, null);
    }
}
