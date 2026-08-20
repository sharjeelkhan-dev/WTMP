package com.sharjeel.wtmp.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(onNavigationToOnboarding: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }
    val currentOnNavigate by rememberUpdatedState(onNavigationToOnboarding)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")

    val isDark = isSystemInDarkTheme()

    // Improved High-Visibility Colors for both modes
    val backgroundCol = if (isDark) Color(0xFF131418) else Color(0xFFF1F5F9)
    val cardBackground = if (isDark) Color(0xFF1E1F25) else Color(0xFFFFFFFF)
    val primaryBlue = Color(0xFF2196F3)
    val textMain = if (isDark) Color.White else Color(0xFF0F172A)
    val textSubtle = if (isDark) Color(0xFF8E8E93) else Color(0xFF475569) // Darker for better visibility

    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlowAlpha"
    )

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(800))
        scale.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        delay(1800.milliseconds)
        currentOnNavigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundCol),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                // Background Soft Blue Radial Glow Effect with higher clarity
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    primaryBlue.copy(alpha = pulseGlowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBackground)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryBlue.copy(alpha = if (isDark) 0.6f else 0.4f),
                                    primaryBlue.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = "WTMP Shield",
                        tint = primaryBlue,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }
            Text(
                text = "WTMP",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 10.sp
                ),
                color = textMain
            )
            Text(
                text = "Who Touched My Phone?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold, // Bolded slightly for crisp visibility
                    letterSpacing = 1.2.sp
                ),
                color = textSubtle
            )
        }
    }
}

// ---------------------------------------------------------
// Previews for Android Studio Design Tab
// ---------------------------------------------------------
@Preview(name = "Splash Screen - Dark", backgroundColor = 0xFF131418, showBackground = true)
@Composable
fun SplashScreenDarkPreview() {
    WTMPTheme(darkTheme = true) {
        SplashScreen(onNavigationToOnboarding = {})
    }
}

@Preview(name = "Splash Screen - Light", backgroundColor = 0xFFF1F5F9, showBackground = true)
@Composable
fun SplashScreenLightPreview() {
    WTMPTheme(darkTheme = false) {
        SplashScreen(onNavigationToOnboarding = {})
    }
}