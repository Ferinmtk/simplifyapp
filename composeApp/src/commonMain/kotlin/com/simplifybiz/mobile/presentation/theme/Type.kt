package com.simplifybiz.mobile.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography uses PlatformFontFamily so iOS gets SF Pro and Android gets
 * Roboto without explicit branching at call sites.
 *
 * Letter-spacing is set to 0 for body styles. Material's default body
 * letter-spacing is positive (0.5), which looks correct on Android but
 * wrong on iOS — Apple's typography is tighter. We split the difference
 * at 0 which is acceptable on both.
 */
val BizTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = PlatformFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = BizBlack,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlatformFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = BizBlack,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlatformFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextSecondary,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlatformFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlatformFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = BizWhite,
        letterSpacing = 0.sp
    )
)