package com.simplifybiz.mobile

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.simplifybiz.mobile.presentation.LoginScreen
import com.simplifybiz.mobile.presentation.DashboardScreen
import com.simplifybiz.mobile.presentation.theme.SimplifyBizTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App(isLoggedIn: Boolean = false) {
    SimplifyBizTheme {
        // choose starting screen based on session manager status
        val startScreen = if (isLoggedIn) DashboardScreen() else LoginScreen()

        Navigator(startScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    App(isLoggedIn = false)
}
