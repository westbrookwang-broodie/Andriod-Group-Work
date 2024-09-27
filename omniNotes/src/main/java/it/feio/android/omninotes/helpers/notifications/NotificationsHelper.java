/*
 * Copyright (C) 2013-2024 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.helpers.notifications;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static it.feio.android.omninotes.helpers.IntentHelper.immutablePendingIntentFlag;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import androidx.activity.ComponentActivity;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.content.ContextCompat;
import com.pixplicity.easyprefs.library.Prefs;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.bus.NotificationsGrantedEvent;
import it.feio.android.omninotes.helpers.BuildHelper;
import lombok.NonNull;


public class NotificationsHelper {

  private final Context mContext;
  private Builder mBuilder;
  private NotificationManager mNotificationManager;

  public NotificationsHelper(Context mContext) {
    this.mContext = mContext.getApplicationContext();
    if (mNotificationManager == null) {
      mNotificationManager = (NotificationManager) mContext
          .getSystemService(Context.NOTIFICATION_SERVICE);
    }
  }

  /**
   * Creates the NotificationChannel, but only on API 26+ because the NotificationChannel class is
   * new and not in the support library
   */
  @TargetApi(VERSION_CODES.O)
  public void initNotificationChannels() {
    if (BuildHelper.isAboveOrEqual(VERSION_CODES.O)) {

      String soundFromPrefs = Prefs.getString("settings_notification_ringtone", null);
      Uri sound = soundFromPrefs != null ? Uri.parse(soundFromPrefs)
          : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

      NotificationChannels.channels.forEach(
          (notificationChannelNames, notificationChannel) -> {
            NotificationChannel channel = new NotificationChannel(notificationChannel.id,
                notificationChannel
                    .name, notificationChannel.importance);
            channel.setDescription(notificationChannel.description);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
            channel.setSound(sound, audioAttributes);

            mNotificationManager.createNotificationChannel(channel);
          });
    }
  }

  @TargetApi(Build.VERSION_CODES.O)
  public void updateNotificationChannelsSound() {
    if (BuildHelper.isAboveOrEqual(VERSION_CODES.O)) {
      Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(Settings.EXTRA_APP_PACKAGE, mContext.getPackageName());
      mContext.startActivity(intent);
    }
  }

  public NotificationsHelper createStandardNotification(
      @NonNull NotificationChannels.NotificationChannelNames channelName, int
      smallIcon, String title, PendingIntent notifyIntent) {
    return createNotification(channelName, smallIcon, title, notifyIntent, false);
  }

  public NotificationsHelper createOngoingNotification(
      @NonNull NotificationChannels.NotificationChannelNames channelName, int
      smallIcon, String title, PendingIntent notifyIntent) {
    return createNotification(channelName, smallIcon, title, notifyIntent, true);
  }

  public NotificationsHelper createNotification(
      @NonNull NotificationChannels.NotificationChannelNames channelName, int
      smallIcon, String title, PendingIntent notifyIntent, boolean isOngoing) {
    mBuilder = new NotificationCompat.Builder(mContext,
        NotificationChannels.channels.get(channelName).id)
        .setSmallIcon(smallIcon)
        .setContentTitle(title)
        .setAutoCancel(!isOngoing)
        .setOngoing(isOngoing)
        .setColor(mContext.getResources().getColor(R.color.colorAccent))
        .setContentIntent(notifyIntent);
    setLargeIcon(R.drawable.logo_notification_lollipop);
    return this;
  }

  public Builder getBuilder() {
    return mBuilder;
  }

  public NotificationsHelper setLargeIcon(Bitmap largeIconBitmap) {
    mBuilder.setLargeIcon(largeIconBitmap);
    return this;
  }

  public NotificationsHelper setLargeIcon(int largeIconResource) {
    Bitmap largeIconBitmap = BitmapFactory
        .decodeResource(mContext.getResources(), largeIconResource);
    return setLargeIcon(largeIconBitmap);
  }

  public NotificationsHelper setRingtone(String ringtone) {
    if (ringtone != null && BuildHelper.isBelow(VERSION_CODES.O)) {
      mBuilder.setSound(Uri.parse(ringtone));
    }
    return this;
  }

  public NotificationsHelper setVibration() {
    return setVibration(null);
  }

  public NotificationsHelper setVibration(long[] pattern) {
    if (pattern == null || pattern.length == 0) {
      pattern = new long[]{500, 500};
    }
    mBuilder.setVibrate(pattern);
    return this;
  }

  public NotificationsHelper setLedActive() {
    mBuilder.setLights(Color.BLUE, 1000, 1000);
    return this;
  }

  public NotificationsHelper setIcon(int icon) {
    mBuilder.setSmallIcon(icon);
    return this;
  }

  public NotificationsHelper setMessage(String message) {
    mBuilder.setContentText(message);
    return this;
  }

  public NotificationsHelper setIndeterminate() {
    mBuilder.setProgress(0, 0, true);
    return this;
  }

  public NotificationsHelper setOngoing() {
    mBuilder.setOngoing(true);
    return this;
  }

  public NotificationsHelper show() {
    show(0);
    return this;
  }

  public NotificationsHelper show(long id) {
    var mNotification = mBuilder.build();
    if (mNotification.contentIntent == null) {
      var emptyExplicitIntent = new Intent(mContext, MainActivity.class);
      var pendingIntent = PendingIntent.getActivity(mContext, 0, emptyExplicitIntent,
          immutablePendingIntentFlag(FLAG_UPDATE_CURRENT));
      mBuilder.setContentIntent(pendingIntent);
    }
    mNotificationManager.notify(String.valueOf(id), 0, mBuilder.build());
    return this;
  }

  public NotificationsHelper start(NotificationChannels.NotificationChannelNames channelName, int
      smallIcon, String title) {
    createStandardNotification(channelName, smallIcon, title, null).setIndeterminate().setOngoing();
    mNotificationManager.notify(0, mBuilder.setOnlyAlertOnce(true).build());
    return this;
  }

  public void updateMessage(String message) {
    updateMessage(0, message);
  }

  public void updateMessage(int id, String message) {
    mNotificationManager.notify(id, mBuilder.setContentText(message).build());
  }

  public void finish(String title, String message) {
    finish(0, title, message);
  }

  public void finish(int id, String title, String message) {
    mBuilder.setContentTitle(title).setContentText(message)
        .setProgress(0, 0, false).setOngoing(false);
    mNotificationManager.notify(id, mBuilder.build());
  }

  public void cancel() {
    mNotificationManager.cancel(0);
  }

  public void cancel(int id) {
    mNotificationManager.cancel(id);
  }

  public boolean checkNotificationsEnabled(Context context) {
    return BuildHelper.isBelow(VERSION_CODES.TIRAMISU)
        || BuildHelper.isDebugBuild()
        || ContextCompat.checkSelfPermission(context, permission.POST_NOTIFICATIONS)
        == PackageManager.PERMISSION_GRANTED;
  }

  public void askToEnableNotifications(ComponentActivity activity) {
    if (BuildHelper.isAboveOrEqual(VERSION_CODES.TIRAMISU)) {
      if (!checkNotificationsEnabled(activity)) {
        activity.registerForActivityResult(new RequestPermission(),
                isGranted -> EventBus.getDefault().post(new NotificationsGrantedEvent(isGranted)))
            .launch(permission.POST_NOTIFICATIONS);
      } else {
        EventBus.getDefault().post(new NotificationsGrantedEvent(true));
      }
    } else {
      EventBus.getDefault().post(new NotificationsGrantedEvent(true));
    }
  }

}
