package com.simplifybiz.mobile.data.auth

// iOS Google Sign-In will be implemented later via the native GoogleSignIn
// SDK from Swift, with the token passed back into Kotlin via a setter.
// For now, return null so the Google-Sign-In button simply shows an
// "iOS Google Sign-In not yet available" error at the UI layer.
actual class GoogleAuthManager {
    actual suspend fun getGoogleIdToken(): String? = null
}