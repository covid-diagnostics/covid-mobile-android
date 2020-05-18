package com.example.coronadiagnosticapp.data.providers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.OkHttpClient
import java.io.File

abstract class PreferenceProvider(context: Context) {
    private val appContext = context.applicationContext

    protected val preferences: SharedPreferences
        get() = appContext.getSharedPreferences(
            "shared",
            Context.MODE_PRIVATE
        )

    val encryptedPrefs =  //by lazy is a property of kotlin- initialization when its called. not on declaration.
        EncryptedSharedPreferences.create(  //allows you to save and read the values that are encrypting and decrypting everything under the hood.
            PREFS_FILENAME,
            masterKeyAlias, // alias created using MasterKeys.getOrCreate(KeyGenParameterSpec)
            appContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, //schemes used for encryption keys and values
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // schemes used for encryption keys and values
        )

    companion object {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) //allows the developer to create a master key and then get an alias for it.
        //A master key that encrypts all keysets. This key is stored using the Android keystore system.
        const val PREFS_FILENAME = "ENCRYPTED_PREFS"
    }
}