package com.example.coronadiagnosticapp.ui.activities.Reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class RegisterNotificationService extends Service {
    private static final String TAG = "RegisterNotification";
    private static final int DAILY_NOTIFICATION_HOUR = 10;
    private static final int DAILY_NOTIFICATION_MINUTE = 0;


    public RegisterNotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setDailyNotification(DAILY_NOTIFICATION_HOUR, DAILY_NOTIFICATION_MINUTE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    /**
     * This function schedules the daily notification.
     */
    private void setDailyNotification(int hour, int minute) {
        Log.i(TAG,"Setting an alarm for - " + hour + ":" + minute);
        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent myIntent;
        PendingIntent pendingIntent;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        myIntent = new Intent(this,AlarmNotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,0,myIntent,0);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}