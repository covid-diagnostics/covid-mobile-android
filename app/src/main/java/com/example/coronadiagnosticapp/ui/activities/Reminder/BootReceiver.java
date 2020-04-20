package com.example.coronadiagnosticapp.ui.activities.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
    /**
     * This will be called when the phone has booted.
     */
    public void onReceive(Context context, Intent intent) {
        Log.d("BootReceive", "Starting RegisterNotificationService.");

        Intent i = new Intent(context, RegisterNotificationService.class);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        }
        else {
            context.startService(i);
        }
        Log.d("BootReceive", "Started RegisterNotificationService.");
    }

}