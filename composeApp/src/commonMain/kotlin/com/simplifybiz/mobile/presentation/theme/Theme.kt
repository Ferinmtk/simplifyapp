package com.simplifybiz.mobile.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BizBlack,
    onPrimary = BizWhite,
    secondary = BizGold,
    background = BizBackground,
    surface = BizWhite,
    onSurface = TextPrimary,
    error = BizError
)

/**
 * Shapes are platform-aware: iOS gets a slightly tighter corner radius
 * because Apple's HIG conventions use ~10dp on cards/dialogs whereas
 * Material defaults to 12-16dp.
 *
 * Components that read MaterialTheme.shapes (Card, Button, AlertDialog,
 * TextField, etc.) will pick this up automatically. Components that hard-
 * code a RoundedCornerShape(12.dp) won't — those are scattered through
 * ObjectiveScreen, ObjectivesListScreen etc. Migrate them gradually to
 * MaterialTheme.shapes.medium where it matters.
 */
private val PlatformShapes: Shapes
    get() {
        val r = PlatformCornerRadius
        return Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small      = RoundedCornerShape(r - 2.dp),
            medium     = RoundedCornerShape(r),
            large      = RoundedCornerShape(r + 4.dp),
            extraLarge = RoundedCornerShape(r + 12.dp)
        )
    }

@Composable
fun SimplifyBizTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = BizTypography,
        shapes = PlatformShapes,
        content = content
    )
}