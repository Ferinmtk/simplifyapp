package com.simplifybiz.mobile.data.auth

/**
 * Platform-specific Google Sign-In handler.
 *
 * On Android, uses Credential Manager + Google ID SDK.
 * On iOS, returns null — iOS Google Sign-In will be added later via the
 * native SDK from Swift and exposed back to Kotlin.
 *
 * No Context parameter in commonMain — Android's actual sources it from
 * Koin's AndroidContext.
 */
expect class GoogleAuthManager {
    suspend fun getGoogleIdToken(): String?
}