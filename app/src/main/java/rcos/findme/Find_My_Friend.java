package rcos.findme;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class Find_My_Friend extends AppCompatActivity implements LocationUpdateCallback {
    private final int CODE_SIZE = 7;
    private final String CODE_NOT_LONG_ENOUGH_ERROR = "Code must be 7 characters in length.";
    private final String CODE_INCORRECT_ERROR = "This code does not match any code available. Please try again.";

    private LocationService locationService;

    private Location location;
    private Location friendLocation;
    private boolean halfway = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if (intent.hasExtra(IntentExtras.HALFWAY)) {
            halfway = intent.getBooleanExtra(IntentExtras.HALFWAY, false);
        }

        if (intent.hasExtra(IntentExtras.LOCATION)) {
            location = intent.getParcelableExtra(IntentExtras.LOCATION);
        }

        locationService = new LocationService(this, this);

        setContentView(R.layout.activity_find_my_friend);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_my_friend, menu);
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
    public void locationUpdated(Location loc) {
        Log.i("Find_My_Friend", "updated location");
        location = loc;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("Find_My_Friend", "Stopped and finished");
        locationService.disconnect();
        finish();
    }

    public void checkCode(View view) {
        EditText editText = (EditText) findViewById(R.id.code);
        String codeToCheck = editText.getText().toString().toLowerCase();

        if(codeToCheck.length() != CODE_SIZE) {
            editText.setError(CODE_NOT_LONG_ENOUGH_ERROR);
            return;
        }

        if(codeExists(codeToCheck)) { findMeMapActivity(view); }
        else {
            editText.setError(CODE_INCORRECT_ERROR);
        }
    }

    // todo: send request to database looking for code.. Should probably grab the Coords in the same request.
    public boolean codeExists(String code) {
        if(code.equals("abc1234")) { return true; }
        return true;
    }

    public void findMeMapActivity(View view) {
        Intent intent = new Intent(this, Find_Me_Map.class);
        friendLocation = location;
        friendLocation.setLongitude(-73.6925);
        friendLocation.setLatitude(42.7317);

        intent.putExtra(IntentExtras.LOCATION, location);
        intent.putExtra(IntentExtras.FRIEND_LOCATION, friendLocation);
        intent.putExtra(IntentExtras.HALFWAY, halfway);
        startActivity(intent);
    }
}
