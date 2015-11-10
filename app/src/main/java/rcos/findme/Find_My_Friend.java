package rcos.findme;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

public class Find_My_Friend extends AppCompatActivity {
    private final String TAG = "Find_My_Friend";
    private final int CODE_SIZE = 7;
    private final String CODE_NOT_LONG_ENOUGH_ERROR = "Code must be 7 characters in length.";
    private final String CODE_INCORRECT_ERROR = "This code does not match any code available. Please try again.";

    private PermissionCheck permissionCheck;

    public final static String LAT_LNG = "rcos.findme.coordinates";
    public final static String PERMISSION = "rcos.findme.permission";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionCheck = new PermissionCheck(this);
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
        if(permissionCheck.CheckLocationPermission() && this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(this, Find_Me_Map.class);

            // default to Troy coordinates
            intent.putExtra(LAT_LNG, new LatLng(42.7317, -73.6925));
            intent.putExtra(PERMISSION, true);
            finish();
            startActivity(intent);
        }
         else {
            finish();
        }
    }
}
