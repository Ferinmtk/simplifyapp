package com.simplifybiz.mobile.util

import androidx.compose.runtime.Composable

// iOS handles dialog keyboard avoidance natively — no SOFT_INPUT equivalent
// needed. This is an intentional no-op.
@Composable
actual fun SecureDialogInputModeEffect() {}