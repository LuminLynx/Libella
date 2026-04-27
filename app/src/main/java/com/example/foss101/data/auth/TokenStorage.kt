package com.example.foss101.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface TokenStorage {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clear()
    fun saveDisplayName(displayName: String?)
    fun getDisplayName(): String?
    fun saveEmail(email: String?)
    fun getEmail(): String?
    fun saveUserId(userId: String?)
    fun getUserId(): String?
}

class EncryptedTokenStorage(context: Context) : TokenStorage {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() }

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    override fun saveDisplayName(displayName: String?) {
        prefs.edit().putString(KEY_DISPLAY_NAME, displayName).apply()
    }

    override fun getDisplayName(): String? = prefs.getString(KEY_DISPLAY_NAME, null)

    override fun saveEmail(email: String?) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    override fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    override fun saveUserId(userId: String?) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    override fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    private companion object {
        const val FILE_NAME = "ai101_auth_prefs"
        const val KEY_TOKEN = "auth_token"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_EMAIL = "email"
        const val KEY_USER_ID = "user_id"
    }
}
