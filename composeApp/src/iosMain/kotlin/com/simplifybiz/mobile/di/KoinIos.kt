package com.simplifybiz.mobile.di

import com.simplifybiz.mobile.data.auth.GoogleAuthManager
import com.simplifybiz.mobile.data.SessionManager
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * iOS-only Koin bootstrap.
 *
 * Registers the iOS GoogleAuthManager stub (which returns null until we add
 * the native iOS Google Sign-In SDK). Swift side must call this once at
 * app launch before the first MainViewController is created — see
 * iosApp/iosApp/ContentView.swift or iOSApp.swift.
 *
 * Also exposes isLoggedIn() so Swift can read the session state without
 * touching Koin directly.
 */
val iosPlatformModule = module {
    single { GoogleAuthManager() }
}

fun initKoinIos() {
    startKoin {
        modules(
            networkModule,
            databaseModule,
            repositoryModule,
            useCaseModule,
            viewModelModule,
            iosPlatformModule
        )
    }
}

/**
 * Swift-friendly session check. Call from Swift to decide which screen to
 * show first — mirrors what MainActivity.onCreate does on Android.
 */
fun isLoggedIn(): Boolean {
    // Lazy import inside the function so Koin is definitely initialised.
    val sessionManager: SessionManager = org.koin.mp.KoinPlatform.getKoin().get()
    return sessionManager.isLoggedIn()
}