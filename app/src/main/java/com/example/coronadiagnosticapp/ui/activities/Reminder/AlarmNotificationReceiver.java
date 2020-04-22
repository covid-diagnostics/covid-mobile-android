package com.example.coronadiagnosticapp.ui.activities.Reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.coronadiagnosticapp.R;
import com.example.coronadiagnosticapp.ui.activities.MainActivity;

import java.util.Locale;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class AlarmNotificationReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    public static final String NOTIFICATION_CHANNEL_NAME = "Daily measurements reminder";

    private static final String TAG = "AlrmNotificationRcvr";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Showing notification.");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        Intent myIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                myIntent,
                FLAG_ONE_SHOT );

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL) // Default light, sound and etc.
                .setWhen(System.currentTimeMillis()) // Set the notification to current time.
                .setSmallIcon(R.drawable.heart) // TODO set a the real app icon.
                .setContentTitle(context.getString(R.string.reminder_notification_title))
                .setContentIntent(pendingIntent) // Open the MainActivity after the user pressed the notification.
                .setContentText(context.getString(R.string.reminder_notification_content));

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Only from version O we need to use NotificationChannel.
            // This allows features like "block all notification like that"
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new
                    NotificationChannel( NOTIFICATION_CHANNEL_ID , NOTIFICATION_CHANNEL_NAME , importance) ;
            builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            notificationManager.createNotificationChannel(notificationChannel) ;
        }

        notificationManager.notify(( int ) System.currentTimeMillis () , builder.build()) ;
    }
}
