package com.simplifybiz.mobile.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.simplifybiz.mobile.BuildConfig

class GoogleAuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun getGoogleIdToken(activityContext: Context): String? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            result.credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
        } catch (e: Exception) {
            println("GOOGLE_AUTH_ERROR_MESSAGE: ${e.message}")
            println("GOOGLE_AUTH_ERROR_CAUSE: ${e.cause}")
            e.printStackTrace()
            null
        }
    }
}
