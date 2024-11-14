package it.feio.android.omninotes.utils;

import static android.widget.Toast.LENGTH_LONG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

public class LocationReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Toast.makeText(context, "Error: " + errorMessage, LENGTH_LONG).show();
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Toast.makeText(context, "Entered location reminder zone!", LENGTH_LONG).show();
            // You could notify user or perform any desired action here
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Toast.makeText(context, "Exited location reminder zone!", LENGTH_LONG).show();
        }
    }
}
