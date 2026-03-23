package com.smartlauncher.data

import android.content.Context

class WalletSessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isConnected(): Boolean = prefs.contains(KEY_AUTH_TOKEN)

    fun save(authToken: String, publicKey: String?) {
        prefs.edit()
            .putString(KEY_AUTH_TOKEN, authToken)
            .putString(KEY_PUBLIC_KEY, publicKey)
            .apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_PUBLIC_KEY)
            .apply()
    }

    fun getPublicKey(): String? = prefs.getString(KEY_PUBLIC_KEY, null)

    companion object {
        private const val PREFS_NAME = "wallet_session"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_PUBLIC_KEY = "public_key"
    }
}
