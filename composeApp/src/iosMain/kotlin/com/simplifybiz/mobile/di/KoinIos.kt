package com.simplifybiz.mobile.di

import com.simplifybiz.mobile.data.SessionManager
import com.simplifybiz.mobile.data.auth.GoogleAuthManager
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * iOS Koin bootstrap — exposed to Swift as `IosKoin.shared`.
 *
 * Using a Kotlin `object` (not top-level functions) because objects export
 * cleanly to Swift as `IosKoin.shared.doInit()` without the
 * `FilenameKt.funcName` indirection, which has been fragile in past builds.
 *
 * Swift usage:
 *   IosKoin.shared.doInit()
 *   let loggedIn = IosKoin.shared.isLoggedIn()
 */
object IosKoin {

    private val iosPlatformModule = module {
        single { GoogleAuthManager() }
    }

    /**
     * Bootstraps Koin for iOS. Must be called once at app launch, before any
     * MainViewController is constructed. Mirrors what
     * SimplifyBizApplication.onCreate does on Android.
     *
     * Named `doInit` instead of `init` because `init` is reserved in Swift —
     * Kotlin/Native would rename it automatically, but explicit is clearer.
     */
    fun doInit() {
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
        val sessionManager: SessionManager = KoinPlatform.getKoin().get()
        return sessionManager.isLoggedIn()
    }
}