package com.simplifybiz.mobile.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BizBlack,
    onPrimary = BizWhite,
    secondary = BizGold,
    background = BizBackground,
    surface = BizWhite,
    onSurface = TextPrimary,
    error = BizError
)

@Composable
fun SimplifyBizTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = BizTypography,
        content = content
    )
}
