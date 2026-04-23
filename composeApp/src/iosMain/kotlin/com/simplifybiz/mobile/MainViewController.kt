package com.simplifybiz.mobile

import androidx.compose.ui.window.ComposeUIViewController
import com.simplifybiz.mobile.di.IosKoin

/**
 * iOS entry point. Called from Swift after `IosKoin.shared.doInit()`.
 *
 * Mirrors Android's MainActivity.onCreate: reads the persisted session
 * state from Room via SessionManager and seeds the Compose tree with it,
 * so users who were already logged in land on the Dashboard instead of
 * the Login screen.
 */
fun MainViewController() = ComposeUIViewController {
    App(isLoggedIn = IosKoin.isLoggedIn())
}