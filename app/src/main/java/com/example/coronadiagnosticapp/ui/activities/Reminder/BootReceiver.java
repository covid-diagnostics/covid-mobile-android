package com.example.coronadiagnosticapp.ui.activities.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver
{
    /**
     * This will be called when the phone has booted.
     */
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, RegisterNotificationService.class);
        context.startService(i);
        Log.d("BootReceive", "Started RegisterNotificationService.");
    }

}