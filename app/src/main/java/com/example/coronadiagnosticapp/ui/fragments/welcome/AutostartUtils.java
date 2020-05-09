package com.example.coronadiagnosticapp.ui.fragments.welcome;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.example.coronadiagnosticapp.BuildConfig;
import com.example.coronadiagnosticapp.R;

import java.util.Arrays;
import java.util.List;

public class AutostartUtils {

    public static void requestAutostartPermissions(Context context) {
        final List<Intent> SETTINGS_INTENTS = Arrays.asList(
                getCompIntent("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"),

                getCompIntent("com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"),

                getCompIntent("com.huawei.systemmanager", Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ?
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                        : "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"),

                getCompIntent("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"),
                getCompIntent("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerSaverModeActivity"),
                getCompIntent("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"),
                getCompIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                        getCompIntent("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")
                        .setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        .setData(Uri.parse("package:" + context.getPackageName())) : null,

                getCompIntent("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),

                getCompIntent("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),

                getCompIntent("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),

                getCompIntent("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),

                getCompIntent("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity"),

                getCompIntent("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity"),

                getCompIntent("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
                        .setData(Uri.parse("mobilemanager://function/entry/AutoStart")),

                getCompIntent("com.meizu.safe", "com.meizu.safe.security.SHOW_APPSEC")
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .putExtra("packageName", BuildConfig.APPLICATION_ID)
        );


        SharedPreferences settings = context.getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE);
        boolean skipMessage = settings.getBoolean("skipProtectedAppCheck", false);
        if (skipMessage) {
            return;
        }

        for (Intent intent : SETTINGS_INTENTS) {
            if (isCallable(context, intent)) {
                saveSkipProtectAppCheck(settings, true);
                showDialog(context, settings, intent);
                return;
            }
        }
    }

    private static Intent getCompIntent(String pkg, String cls) {
        ComponentName componentName = new ComponentName(pkg, cls);
        return new Intent().setComponent(componentName);
    }

    private static void showDialog(Context context, SharedPreferences settings, Intent intent) {
        final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(context);
        dontShowAgain.setText(context.getString(R.string.dont_show_again));
        dontShowAgain.setOnCheckedChangeListener((v, isChecked) -> {
            saveSkipProtectAppCheck(settings, isChecked);
        });

        String appName = context.getString(R.string.app_name);
        String msg = context.getString(R.string.autostart_dialog_content, appName);
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.autostart_dialog_title))
                .setMessage(msg)
                .setView(dontShowAgain)
                .setPositiveButton(context.getString(R.string.settings), (d, which) -> {
                    context.startActivity(intent);
                    // Don't show this message again.
                    saveSkipProtectAppCheck(settings, true);
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }

    private static void saveSkipProtectAppCheck(SharedPreferences preferences, boolean isChecked) {
        preferences.edit()
                .putBoolean("skipProtectedAppCheck", isChecked)
                .apply();
    }

    private static boolean isCallable(Context context, Intent intent) {
        try {
            if (intent == null || context == null) {
                return false;
            }
            
            List<ResolveInfo> list = 
                    context
                    .getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            
            return !list.isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }
}
