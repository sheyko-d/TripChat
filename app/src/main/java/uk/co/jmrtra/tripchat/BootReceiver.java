package uk.co.jmrtra.tripchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    private SharedPreferences mPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (mPrefs.getInt("daily_notif", -1) != today) {
            showNotification(context);
            mPrefs.edit().putInt("daily_notif", today).apply();
        }
    }

    private void showNotification(final Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder
                (context).setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("Traversation")
                .setContentText("Chat to someone new on your next travel")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        if (mPrefs.getBoolean("sound", true)) {
            notificationBuilder.setSound(defaultSoundUri);
        }

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        Notification note = notificationBuilder.build();
        if (mPrefs.getBoolean("vibrate", true)) {
            note.defaults |= Notification.DEFAULT_VIBRATE;
        }

        notificationManager.notify(0, note);

    }
}
