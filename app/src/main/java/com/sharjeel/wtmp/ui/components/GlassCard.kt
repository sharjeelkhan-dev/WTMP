package com.sharjeel.wtmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Glassmorphism Card Container.
 * Applies a semi-transparent background with a subtle vertical gradient border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glassShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .clip(glassShape)
            .background(Color.White.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = glassShape
            ),
        content = content
    )
}

/**
 * Premium Stripe / iOS Cupertino Grade Bento Card with soft ambient shadow and ultra-clean border.
 */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF1E293B) else Color.White
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 6.dp else 8.dp,
                shape = shape,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color(0xFF64748B).copy(alpha = 0.12f),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.8f) else Color(0xFF64748B).copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0),
                shape = shape
            )
            .then(clickModifier),
        content = content
    )
}
