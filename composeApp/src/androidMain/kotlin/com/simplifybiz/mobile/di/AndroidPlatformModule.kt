package com.simplifybiz.mobile.di

import com.simplifybiz.mobile.data.auth.GoogleAuthManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-only Koin module.
 *
 * Registers the platform-specific GoogleAuthManager that needs an android
 * Context. Add to the Koin graph from SimplifyBizApplication.onCreate.
 */
val androidPlatformModule = module {
    single { GoogleAuthManager(androidContext()) }
}
