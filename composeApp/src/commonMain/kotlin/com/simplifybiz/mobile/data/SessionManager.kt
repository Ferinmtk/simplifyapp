package com.simplifybiz.mobile.data

import com.russhwolf.settings.Settings
class SessionManager(
    private val settings: Settings
) {
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    fun saveAuthToken(token: String) {
        settings.putString(KEY_AUTH_TOKEN, token)
    }

    fun getAuthToken(): String? {
        return settings.getStringOrNull(KEY_AUTH_TOKEN)
    }

    fun clearSession() {
        settings.remove(KEY_AUTH_TOKEN)
    }

    fun isLoggedIn(): Boolean {
        return settings.getStringOrNull(KEY_AUTH_TOKEN) != null
    }
}
