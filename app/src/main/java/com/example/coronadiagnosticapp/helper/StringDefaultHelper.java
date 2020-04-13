package com.example.coronadiagnosticapp.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

import java.util.Arrays;
import java.util.Locale;

public class StringDefaultHelper {
    public static String getDefaultString(Context context, @StringRes int stringId) {
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        Locale defaultLocale = new Locale("en"); // default locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(defaultLocale);
            configuration.setLocales(localeList);
            return context.createConfigurationContext(configuration).getString(stringId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(defaultLocale);
            return context.createConfigurationContext(configuration).getString(stringId);
        }
        return context.getString(stringId);
    }


    public static String[] getDefaultArrayString(Context context, @ArrayRes int arrayRes) {
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        Locale defaultLocale = new Locale("en"); // default locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(defaultLocale);
            configuration.setLocales(localeList);
            return context.createConfigurationContext(configuration).getResources().getStringArray(arrayRes);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(defaultLocale);
            return context.createConfigurationContext(configuration).getResources().getStringArray(arrayRes);
        }
        return context.getResources().getStringArray(arrayRes);
    }

    public static int getItemPosition(String[] engArray, String engStringItem) {
        int position = Arrays.asList(engArray).indexOf(engStringItem);
        if (position == -1) return 0;
        return position;
    }
}
