package rcos.findme;

import android.content.Intent;
import android.graphics.Camera;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Find_Me_Map extends AppCompatActivity implements LocationUpdateCallback {

    public static final String TAG = Find_Me_Map.class.getSimpleName();

    private GoogleMap map;
    private LocationService locationService;
    private boolean halfway;

    private Location location;
    private LatLng latlng;

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
        }

        friendLocation = intent.getParcelableExtra(IntentExtras.FRIEND_LOCATION);
        if (friendLocation != null) {
            friendLatLng = new LatLng(friendLocation.getLatitude(), friendLocation.getLongitude());
        }

        halfway = intent.getBooleanExtra(IntentExtras.HALFWAY, false);

        setContentView(R.layout.activity_find_me_map);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void locationUpdated(Location loc) {
        location = loc;
        latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
        userMarker.setPosition(latlng);
        Log.i("LocationUpdated", "changed marker position");
    }

    private void addMarkersToMap() {
        map.clear();
        userMarker = map.addMarker(new MarkerOptions().position(latlng));
        Log.i("AddMarkers", "added user marker");
        friendMarker = map.addMarker(new MarkerOptions().position(friendLatLng).title("Friend's Position"));
        Log.i("AddMarkers", "added friend marker");
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
