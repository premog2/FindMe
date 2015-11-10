package rcos.findme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;

public class Home extends AppCompatActivity {

    private PermissionCheck permissionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionCheck = new PermissionCheck(this);
        setContentView(R.layout.activity_home);
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

    public void findMyFriendActivity(View view) {
        Intent intent = new Intent(this, Find_My_Friend.class);
        startActivity(intent);
    }

    public void friendFindMeActivity(View view) {
        boolean permissionGranted = permissionCheck.CheckLocationPermission();

        if(permissionGranted) {
            Intent intent = new Intent(this, Friend_Find_Me.class);
            startActivity(intent);
        }
    }

    public void meetHalfwayActivity(View view) {
        
    }

    public void findMeMapActivity(View view) {
        Intent intent = new Intent(this, Find_Me_Map.class);
        startActivity(intent);
    }
}
