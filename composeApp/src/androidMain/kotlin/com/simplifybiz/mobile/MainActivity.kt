package com.simplifybiz.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.simplifybiz.mobile.data.SessionManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    // inject session manager to check login status
    private val sessionManager: SessionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // pass login status to the App function
            App(isLoggedIn = sessionManager.isLoggedIn())
        }
    }
}
