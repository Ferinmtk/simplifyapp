package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import simplifybiz.composeapp.generated.resources.Res
import simplifybiz.composeapp.generated.resources.logo_zen_stones

class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        LaunchedEffect(Unit) {
            // Stay on the logo for 1.5 seconds
            delay(1500)
            // Move to Login
            navigator?.replace(LoginScreen())
        }

        // Static UI Layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_zen_stones),
                contentDescription = "SimplifyBiz Logo",
                modifier = Modifier.size(150.dp)
            )
        }
    }
}
