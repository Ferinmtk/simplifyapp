package com.simplifybiz.mobile.util

import androidx.compose.runtime.Composable

/**
 * Platform-specific Compose dialog input-mode tweak.
 *
 * On Android, forces the dialog's soft input mode to SOFT_INPUT_ADJUST_RESIZE
 * so the full keyboard (not a floating one) appears inside Dialog windows.
 * On iOS, no-op — iOS handles keyboard avoidance natively.
 *
 * Call this once at the top of any Dialog { } whose TextFields should get
 * the full keyboard treatment. Replaces the old inline LocalView +
 * DialogWindowProvider + SOFT_INPUT_ADJUST_RESIZE block that only worked on
 * Android.
 */
@Composable
expect fun SecureDialogInputModeEffect()