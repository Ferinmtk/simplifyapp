package com.simplifybiz.mobile.presentation.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp

/**
 * Platform-varying theme values.
 *
 * Each platform supplies its own actual to give the app a more native feel
 * without forking the whole UI. Keep this surface SMALL — every value here
 * has to be implemented twice. Only add things that visibly differ on iOS
 * (typography, corner roundness, paddings, etc.).
 */

/** System sans-serif — SF Pro on iOS, Roboto on Android. */
expect val PlatformFontFamily: FontFamily

/**
 * Default corner radius for cards, dialogs, and rounded surfaces.
 * iOS conventions sit around 10-12dp; Android Material defaults to 12-16dp.
 */
expect val PlatformCornerRadius: Dp

/** Whether the runtime is iOS — handy for one-off behavioral switches. */
expect val isIOS: Boolean