package com.example.coronadiagnosticapp.ui.fragments.welcome

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.widget.AppCompatCheckBox
import com.example.coronadiagnosticapp.BuildConfig
import com.example.coronadiagnosticapp.R

object AutostartUtils {
    fun requestAutostartPermissions(context: Context) {
        val SETTINGS_INTENTS = createSettingIntents()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent =
                getCompIntent("com.coloros.safecenter", "startupapp.StartupAppListActivity")
                    .setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)//TODO check this
                    .setData(Uri.parse("package:" + context.packageName))
            SETTINGS_INTENTS += intent
        }

        val settings =
            context.getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE)
        val skipMessage = settings.getBoolean("skipProtectedAppCheck", false)
        if (skipMessage) {
            return
        }

        SETTINGS_INTENTS.firstOrNull { isCallable(context, it) }?.let {
            saveSkipProtectAppCheck(settings, true)
            showDialog(context, settings, it)
        }
    }

    private fun createSettingIntents(): MutableList<Intent> {
        return mutableListOf(
            getCompIntent(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity",
                false
            ),
            getCompIntent(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity",
                false
            ),
            getCompIntent(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerSaverModeActivity",
                false
            ),
            getCompIntent(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity",
                false
            ),
            getCompIntent("com.huawei.systemmanager", "optimize.process.ProtectActivity"),
            getCompIntent("com.coloros.safecenter", "permission.startup.StartupAppListActivity"),
            getCompIntent("com.oppo.safe", "permission.startup.StartupAppListActivity"),
            getCompIntent("com.iqoo.secure", "ui.phoneoptimize.AddWhiteListActivity"),
            getCompIntent("com.iqoo.secure", "ui.phoneoptimize.BgStartUpManager"),
            getCompIntent("com.vivo.permissionmanager", "activity.BgStartUpManagerActivity"),
            getCompIntent("com.asus.mobilemanager", "entry.FunctionActivity"),
            getCompIntent("com.asus.mobilemanager", "autostart.AutoStartActivity"),
            getCompIntent("com.letv.android.letvsafe", "AutobootManageActivity")
                .setData(Uri.parse("mobilemanager://function/entry/AutoStart")),
            getCompIntent(
                "com.huawei.systemmanager",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    "startupmgr.ui.StartupNormalAppListActivity"
                else
                    "appcontrol.activity.StartupAppControlActivity"
            ),
            getCompIntent("com.meizu.safe", "security.SHOW_APPSEC")
                .addCategory(Intent.CATEGORY_DEFAULT)
                .putExtra("packageName", BuildConfig.APPLICATION_ID)
        )
    }

    private fun getCompIntent(pkg: String, cls: String, samePackage: Boolean = true): Intent {
        val newCls = if (samePackage) "$pkg.$cls" else cls
        val componentName = ComponentName(pkg, newCls)
        return Intent().setComponent(componentName)
    }

    private fun showDialog(context: Context, settings: SharedPreferences, intent: Intent) {
        val dontShowAgain = AppCompatCheckBox(context).apply {
            setText(R.string.dont_show_again)
            setOnCheckedChangeListener { _, isChecked ->
                saveSkipProtectAppCheck(settings, isChecked)
            }
        }
        val msg = context.getString(
            R.string.autostart_dialog_content,
            context.getString(R.string.app_name)
        )
        AlertDialog.Builder(context)
            .setTitle(R.string.autostart_dialog_title)
            .setMessage(msg)
            .setView(dontShowAgain)
            .setPositiveButton(R.string.settings) { _, _ ->
                context.startActivity(intent)
                // Don't show this message again.
                saveSkipProtectAppCheck(settings, true)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun saveSkipProtectAppCheck(preferences: SharedPreferences, isChecked: Boolean) =
        preferences.edit()
            .putBoolean("skipProtectedAppCheck", isChecked)
            .apply()

    private fun isCallable(context: Context, intent: Intent) = try {
        context.packageManager
            .queryIntentActivities(intent, MATCH_DEFAULT_ONLY)
            .isNotEmpty()
    } catch (ignored: Exception) {
        false
    }
}