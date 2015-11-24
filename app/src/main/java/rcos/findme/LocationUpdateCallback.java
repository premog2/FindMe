package rcos.findme;

import android.location.Location;

/**
 * Created by Garret on 11/24/2015.
 */
public interface LocationUpdateCallback {
    void locationUpdated(Location location);
}
