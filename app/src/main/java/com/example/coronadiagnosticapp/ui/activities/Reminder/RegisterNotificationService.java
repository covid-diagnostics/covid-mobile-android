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

    public RegisterNotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "oncreate RegisterNotificationService.");
        Toast.makeText(
                this,
                "oncreate RegisterNotification",
                Toast.LENGTH_LONG).show();

        super.onCreate();
        startAlarm();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    /**
     * This function schedules the daily notification.
     */
    private void startAlarm() {
        Log.d(TAG,"string an alarm");
        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent myIntent;
        PendingIntent pendingIntent;

        Calendar calendar= Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 39);
        //calendar.set(Calendar.HOUR_OF_DAY, 10);
        //calendar.set(Calendar.MINUTE, 0);

        myIntent = new Intent(this,AlarmNotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,0,myIntent,0);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}