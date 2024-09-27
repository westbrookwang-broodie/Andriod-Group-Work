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

package it.feio.android.omninotes.utils;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.widget.Toast.LENGTH_LONG;
import static it.feio.android.omninotes.helpers.IntentHelper.immutablePendingIntentFlag;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.date.DateUtils;
import java.util.Calendar;


public class ReminderHelper {

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

}
