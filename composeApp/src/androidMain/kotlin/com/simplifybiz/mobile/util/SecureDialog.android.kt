package com.simplifybiz.mobile.util

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

@Composable
actual fun SecureDialogInputModeEffect() {
    val view = LocalView.current
    SideEffect {
        (view.parent as? DialogWindowProvider)?.window
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
}