package it.feio.android.omninotes.receiver;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static it.feio.android.omninotes.helpers.IntentHelper.immutablePendingIntentFlag;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_POSTPONE;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SNOOZE;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_FILES;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.List;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.SnoozeActivity;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.IntentHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.notifications.NotificationChannels;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.services.NotificationListener;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.ParcelableUtil;
import it.feio.android.omninotes.utils.TextHelper;

public class GeofenceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.hasExtra(INTENT_NOTE)) {
                Note note = ParcelableUtil.unmarshall(intent.getExtras().getByteArray(INTENT_NOTE), Note
                        .CREATOR);
                Context activityContext = OmniNotes.getCurrentActivity();
                sendNotification(activityContext, note);
                showDialog(activityContext, note);
//                SnoozeActivity.setNextRecurrentReminder(note);
                updateNote(note);
            }
        } catch (Exception e) {
            LogDelegate.e("Error on receiving reminder", e);
        }
    }
//        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
//        if (geofencingEvent.hasError()) {
//            Log.e("GeofenceReceiver", "Geofence event error: " + geofencingEvent.getErrorCode());
//            return;
//        }

//+


    private void updateNote(Note note) {
        note.setArchived(false);
        if (!NotificationListener.isRunning()) {
            note.setReminderFired(true);
        }
        DbHelper.getInstance().updateNote(note, false);
    }

//    private void createNotification(Context mContext, Note note) {
//        PendingIntent piSnooze = IntentHelper
//                .getNotePendingIntent(mContext, SnoozeActivity.class, ACTION_SNOOZE, note);
//        PendingIntent piPostpone = IntentHelper
//                .getNotePendingIntent(mContext, SnoozeActivity.class, ACTION_POSTPONE, note);
//        PendingIntent notifyIntent = IntentHelper
//                .getNotePendingIntent(mContext, SnoozeActivity.class, null, note);
//
//        Spanned[] titleAndContent = TextHelper.parseTitleAndContent(mContext, note);
//        String title = TextHelper.getAlternativeTitle(mContext, note, titleAndContent[0]);
//        String text = titleAndContent[1].toString();
//
//        NotificationsHelper notificationsHelper = new NotificationsHelper(mContext);
//        notificationsHelper.createStandardNotification(NotificationChannels.NotificationChannelNames.REMINDERS,
//                R.drawable.ic_stat_notification,
//                title, notifyIntent).setLedActive().setMessage(text);
//
//        List<Attachment> attachments = note.getAttachmentsList();
//        if (!attachments.isEmpty() && !attachments.get(0).getMime_type().equals(MIME_TYPE_FILES)) {
//            Bitmap notificationIcon = BitmapHelper
//                    .getBitmapFromAttachment(mContext, note.getAttachmentsList().get(0), 128,
//                            128);
//            notificationsHelper.setLargeIcon(notificationIcon);
//        }
//
//        String snoozeDelay = Prefs.getString("settings_notification_snooze_delay", "10");
//
//        notificationsHelper.getBuilder()
//                .addAction(R.drawable.location_choose,
//                        TextHelper.capitalize(mContext.getString(R.string.snooze)) + ": " + snoozeDelay,
//                        piSnooze)
//                .addAction(R.drawable.ic_remind_later_light,
//                        TextHelper.capitalize(mContext.getString(R.string
//                                .add_reminder)), piPostpone);
//
//        setRingtone(notificationsHelper);
//        setVibrate(notificationsHelper);
//
//        notificationsHelper.show(note.get_id());
//    }

    private void setRingtone(NotificationsHelper notificationsHelper) {
        String ringtone = Prefs.getString("settings_notification_ringtone", null);
        notificationsHelper.setRingtone(ringtone);
    }


    private void setVibrate(NotificationsHelper notificationsHelper) {
        if (Prefs.getBoolean("settings_notification_vibration", true)) {
            notificationsHelper.setVibration();
        }
    }

    public void sendNotification(Context context, Note note) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Channel for all notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("default_channel_id", name, importance);
            channel.setDescription(description);

            // 获取系统 NotificationManager 服务
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default_channel_id")
                .setSmallIcon(R.drawable.location_choose)
                .setContentTitle(note.getTitle())
                .setContentText(note.getContent())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 创建一个点击通知后打开 MainActivity 的 Intent
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, immutablePendingIntentFlag(FLAG_CANCEL_CURRENT));

        builder.setContentIntent(pendingIntent);

        // 获取 NotificationManager 实例
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 发送通知
        Log.d("NotificationTest", "Sending notification...");
        if (notificationManager != null) {
            notificationManager.notify(121, builder.build());
        }
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, immutablePendingIntentFlag(FLAG_CANCEL_CURRENT));


    }



    public void showDialog(Context context, Note note) {
        // 确保传入的是一个 UI Context (比如 Activity 或通过 context.getApplicationContext())
        if (context != null && context instanceof Activity) {
            // 创建AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(note.getTitle())
                    .setMessage(note.getContent())
                    .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 点击“确定”按钮后执行的代码
                            dialog.dismiss();  // 关闭弹窗
                        }
                    })
                    .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 点击“取消”按钮后执行的代码
                            dialog.dismiss();  // 关闭弹窗
                        }
                    });

            // 显示弹窗
            builder.show();
        }
    }
}


//}
