package it.feio.android.omninotes.utils;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.widget.Toast.LENGTH_LONG;
import static it.feio.android.omninotes.helpers.IntentHelper.immutablePendingIntentFlag;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.receiver.GeofenceReceiver;
import it.feio.android.omninotes.utils.date.DateUtils;
import java.util.Calendar;
import java.util.Collections;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class ReminderHelper {

  private static final float GEOFENCE_RADIUS_IN_METERS = 100;  // Radius for geofence trigger
  private static GeofencingClient geofencingClient;

  private ReminderHelper() {
    // hides public constructor
  }

  public static void addReminder(Context context, Note note) {
    if (note.getAlarm() != null) {
      addReminder(context, note, Long.parseLong(note.getAlarm()));
    }
  }

  public static void addReminder(Context context, Note note, long reminder) {
    if (DateUtils.isFuture(reminder)) {
      Intent intent = new Intent(context, AlarmReceiver.class);
      intent.putExtra(INTENT_NOTE, ParcelableUtil.marshall(note));
      PendingIntent sender = PendingIntent.getBroadcast(context, getRequestCode(note), intent,
              immutablePendingIntentFlag(FLAG_CANCEL_CURRENT));
      AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      am.setExact(AlarmManager.RTC_WAKEUP, reminder, sender);
    }
  }

  /**
   * Checks if exists any reminder for given note
   */
  public static boolean checkReminder(Context context, Note note) {
    return
            PendingIntent.getBroadcast(context, getRequestCode(note), new Intent(context, AlarmReceiver
                    .class), immutablePendingIntentFlag(FLAG_NO_CREATE)) != null;
  }

  static int getRequestCode(Note note) {
    long longCode = note.getCreation() != null ? note.getCreation()
            : Calendar.getInstance().getTimeInMillis() / 1000L;
    return (int) longCode;
  }

  public static void removeReminder(Context context, Note note) {
    if (!TextUtils.isEmpty(note.getAlarm())) {
      AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      Intent intent = new Intent(context, AlarmReceiver.class);
      PendingIntent p = PendingIntent.getBroadcast(context, getRequestCode(note), intent,
              immutablePendingIntentFlag(0));
      am.cancel(p);
      p.cancel();
    }
  }

  public static void showReminderMessage(String reminderString) {
    if (reminderString != null) {
      var context = OmniNotes.getAppContext();
      long reminder = Long.parseLong(reminderString);
      if (reminder > Calendar.getInstance().getTimeInMillis()) {
        new Handler(OmniNotes.getAppContext().getMainLooper()).post(() ->
                Toast.makeText(context,
                        context.getString(R.string.alarm_set_on) + " " + DateHelper
                                .getDateTimeShort(context, reminder), LENGTH_LONG).show());

        if (!new NotificationsHelper(context).checkNotificationsEnabled(context)) {
          Toast.makeText(context, context.getString(R.string.denied_notifications_permission), LENGTH_LONG).show();
        }
      }
    }
  }

  public static void init(Context context) {
    geofencingClient = LocationServices.getGeofencingClient(context);
  }

  private static boolean hasLocationPermission(Context context) {
    return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  public static void showNotification(Context context, String message) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    Notification notification = new NotificationCompat.Builder(context, "location_reminder_channel")
            .setContentTitle("Geofence Notification")
            .setContentText(message)
            .setSmallIcon(R.drawable.location_choose)
            .build();

    notificationManager.notify(1, notification);
  }


  public static void addLocationReminder(Context context, Note note, double latitude, double longitude) {
//    if (!(context instanceof Activity)) {
//      // You can either pass an Activity context or show a Toast elsewhere
//      Log.e("LocationReminder", "Context must be an Activity context.");
//      return;
//    }

    if (!hasLocationPermission(context)) {
      Toast.makeText(context, "Location permission is required to set location reminders.", Toast.LENGTH_LONG).show();
      return;
    }

    if (geofencingClient == null) {
      init(context);
    }

    Geofence geofence = new Geofence.Builder()
            .setRequestId(String.valueOf(getRequestCode(note)))
            .setCircularRegion((float) latitude, (float) longitude, GEOFENCE_RADIUS_IN_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
            .build();

    Intent intent = new Intent(context,  GeofenceReceiver.class);
    intent.putExtra(INTENT_NOTE, ParcelableUtil.marshall(note));
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, getRequestCode(note), intent, immutablePendingIntentFlag(FLAG_CANCEL_CURRENT));

    GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build();

    try {
      geofencingClient.addGeofences(geofencingRequest, pendingIntent)
              .addOnSuccessListener(aVoid -> {
                Log.d("Geofence", "Geofence added successfully");
              })
              .addOnFailureListener(e -> {
                Log.e("Geofence", "Error adding geofence", e);
              });
    } catch (SecurityException e) {
      showNotification(context, "Location permissions are missing for geofences.");
    }
  }

  public static void removeLocationReminder(Context context, Note note) {
    if (!hasLocationPermission(context)) {
      Toast.makeText(context, "Location permission is required to remove location reminders.", LENGTH_LONG).show();
      return;
    }

    if (geofencingClient == null) {
      init(context);
    }

    try {
      geofencingClient.removeGeofences(Collections.singletonList(String.valueOf(getRequestCode(note))))
              .addOnSuccessListener(aVoid ->
                      Toast.makeText(context, "Location reminder removed successfully", LENGTH_LONG).show())
              .addOnFailureListener(e ->
                      Toast.makeText(context, "Failed to remove location reminder: " + getErrorString(e), LENGTH_LONG).show());
    } catch (SecurityException e) {
      Toast.makeText(context, "Location permissions are missing for geofences.", LENGTH_LONG).show();
    }
  }

  private static PendingIntent getGeofencePendingIntent(Context context) {
    Intent intent = new Intent(context, LocationReminderReceiver.class);
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | immutablePendingIntentFlag(0));
  }

  private static String getErrorString(Exception e) {
    if (e instanceof ApiException) {
      ApiException apiException = (ApiException) e;
      switch (apiException.getStatusCode()) {
        case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
          return "Geofence service is not available now.";
        case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
          return "Too many geofences created.";
        case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
          return "Too many pending intents.";
      }
    }
    return e.getMessage();
  }
}
