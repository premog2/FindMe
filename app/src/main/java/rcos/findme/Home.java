package rcos.findme;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class Home extends AppCompatActivity implements LocationUpdateCallback {

    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        locationService = new LocationService(this, this);
        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(locationService != null) {
            if (locationService.isGoogleApiClientConnected() && !locationService.isRequestingLocationUpdates()) {
                locationService.startLocationUpdates();
            } else if (!locationService.isGoogleApiClientConnected()) {
                locationService.attemptConnection();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationService.disconnect();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(SavedInstanceStateKeys.REQUESTING_LOCATION_UPDATES_KEY, locationService.isRequestingLocationUpdates());
        savedInstanceState.putParcelable(SavedInstanceStateKeys.LOCATION_KEY, locationService.getCurrentLocation());
        savedInstanceState.putString(SavedInstanceStateKeys.LAST_UPDATED_TIME_KEY, locationService.getLastUpdateTime());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void locationUpdated(Location location) {
        String update = "";
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        String lats = Double.toString((lat));
        String lngs = Double.toString((lng));

        update = update + lats + ", " + lngs;

        Log.i("Update String", update);

        TextView t = (TextView)findViewById(R.id.welcomeToFindMeText);
        t.setText(update);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // update data based on data saved in onSaveInstanceState
            if (savedInstanceState.keySet().contains(SavedInstanceStateKeys.REQUESTING_LOCATION_UPDATES_KEY)) {
                locationService.setRequestingLocationUpdates(
                        savedInstanceState.getBoolean(SavedInstanceStateKeys.REQUESTING_LOCATION_UPDATES_KEY));
            }

            if (savedInstanceState.keySet().contains(SavedInstanceStateKeys.LOCATION_KEY)) {
                locationService.setCurrentLocation(
                        (Location) savedInstanceState.getParcelable(SavedInstanceStateKeys.LOCATION_KEY));
            }

            if (savedInstanceState.keySet().contains(SavedInstanceStateKeys.LAST_UPDATED_TIME_KEY)) {
                locationService.setLastUpdateTime(
                        savedInstanceState.getString(SavedInstanceStateKeys.LAST_UPDATED_TIME_KEY));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ErrorCodes.REQUEST_RESOLVE_ERROR) {
            locationService.setResolvingError(false);
            if (resultCode == RESULT_OK) {
                if(!locationService.isGoogleApiClientConnecting() && !locationService.isGoogleApiClientConnected() ){
                    locationService.attemptConnection();
                }
            }
        }
    }

    public void findMyFriendActivity(View view) {
        Intent intent = new Intent(this, Find_My_Friend.class);
        startActivity(intent);
    }

    public void friendFindMeActivity(View view) {
            Intent intent = new Intent(this, Friend_Find_Me.class);

            // todo: get device coordinates and send to backend successfully before starting activity
            intent.putExtra(IntentExtras.LAT_LNG, new LatLng(42.7317, -73.6925));
            startActivity(intent);
    }

    public void meetHalfwayActivity(View view) {
        Intent intent = new Intent(this, Generate_Or_Receive.class);
        startActivity(intent);
    }

    public void findMeMapActivity(View view) {
        Intent intent = new Intent(this, Find_Me_Map.class);
        startActivity(intent);
    }
}
