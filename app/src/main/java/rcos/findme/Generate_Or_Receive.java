package rcos.findme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

public class Generate_Or_Receive extends AppCompatActivity {

    private PermissionCheck permissionCheck;
    private IntentExtras intentExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionCheck = new PermissionCheck(this);
        setContentView(R.layout.activity_generate_or_receive);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_generate_or_receive, menu);
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

    public void generateCodeActivity(View view) {
        if(permissionCheck.CheckLocationPermission()) {
            Intent intent = new Intent(this, Friend_Find_Me.class);

            // todo: get devices actual coordinates
            intent.putExtra(intentExtras.LAT_LNG, new LatLng(42.7317, -73.6925));
            intent.putExtra(intentExtras.HALFWAY, true);
            finish();
            startActivity(intent);
        }
    }

    public void receiveCodeActivity(View view) {
        Intent intent = new Intent(this, Find_My_Friend.class);

        intent.putExtra(intentExtras.HALFWAY, true);
        finish();
        startActivity(intent);
    }
}
