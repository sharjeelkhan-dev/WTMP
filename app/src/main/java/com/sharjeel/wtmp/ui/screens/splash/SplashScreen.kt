package com.sharjeel.wtmp.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// =================================================================
// 1. SPLASH SCREEN COMPOSABLE
// =================================================================

/**
 * Animated splash screen featuring staggered element entry, ambient pulsing,
 * and a smooth scale-out transition before navigating to the Onboarding flow.
 */
@Composable
fun SplashScreen(onNavigationToOnboarding: () -> Unit) {
    val currentOnNavigate by rememberUpdatedState(onNavigationToOnboarding)
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    // --- ANIMATION STATES ---

    // Screen Exit Transition States
    val screenAlpha = remember { Animatable(0f) }
    val screenScale = remember { Animatable(0.92f) }

    // Staggered Component Animations
    val iconScale = remember { Animatable(0.4f) }
    val iconAlpha = remember { Animatable(0f) }

    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(20f) }

    val subtitleAlpha = remember { Animatable(0f) }
    val subtitleOffsetY = remember { Animatable(15f) }

    // Color Palette Assignments
    val backgroundCol = colorScheme.background
    val cardBackground = colorScheme.surface
    val themePrimary = colorScheme.primary
    val textMain = colorScheme.onBackground
    val textSubtle = colorScheme.onBackground.copy(alpha = 0.7f)

    // Ambient Continuous Background Pulses
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_pulse")

    val outerPulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerPulseGlow"
    )

    val innerPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerPulseScale"
    )

    // --- ANIMATION CHOREOGRAPHY ---

    LaunchedEffect(Unit) {
        // Step 1: Screen & Icon Entry
        launch { screenAlpha.animateTo(1f, tween(400)) }
        launch {
            iconAlpha.animateTo(1f, tween(300))
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Step 2: Main Title Entrance
        delay(350.milliseconds)
        launch { textAlpha.animateTo(1f, tween(400)) }
        launch { textOffsetY.animateTo(0f, tween(400, easing = LinearOutSlowInEasing)) }

        // Step 3: Subtitle Entrance
        delay(150.milliseconds)
        launch { subtitleAlpha.animateTo(1f, tween(400)) }
        launch { subtitleOffsetY.animateTo(0f, tween(400, easing = LinearOutSlowInEasing)) }

        // Step 4: Display Hold & Exit Transition
        delay(1500.milliseconds)
        launch { screenAlpha.animateTo(0f, tween(350)) }
        launch { screenScale.animateTo(1.08f, tween(350, easing = FastOutSlowInEasing)) }

        // Step 5: Trigger Navigation Callback
        delay(350.milliseconds)
        currentOnNavigate()
    }

    // --- UI LAYOUT ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundCol)
            .alpha(screenAlpha.value)
            .scale(screenScale.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo Container with Glowing Radial Ripples
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
            ) {
                // Outer Radial Pulse Glow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(innerPulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    themePrimary.copy(alpha = outerPulseGlow),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Inner Soft Radial Accent
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    themePrimary.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Glassmorphic App Icon Surface
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(cardBackground)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    themePrimary.copy(alpha = if (isDark) 0.7f else 0.45f),
                                    themePrimary.copy(alpha = 0.08f)
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.webcam_icon),
                        contentDescription = "WTMP CCTV",
                        tint = themePrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Main Brand Title
            Text(
                text = "WTMP",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 10.sp
                ),
                color = textMain,
                modifier = Modifier
                    .offset(y = textOffsetY.value.dp)
                    .alpha(textAlpha.value)
            )

            // Application Subtitle
            Text(
                text = "Who Touched My Phone?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                ),
                color = textSubtle,
                modifier = Modifier
                    .offset(y = subtitleOffsetY.value.dp)
                    .alpha(subtitleAlpha.value)
            )
        }
    }
}

// =================================================================
// 2. PREVIEW PROVIDERS
// =================================================================

@Preview(name = "Splash - Dark", showBackground = true)
@Composable
fun SplashScreenDarkPreview() {
    WTMPTheme(darkTheme = true) {
        SplashScreen(onNavigationToOnboarding = {})
    }
}

@Preview(name = "Splash - Light", showBackground = true)
@Composable
fun SplashScreenLightPreview() {
    WTMPTheme(darkTheme = false) {
        SplashScreen(onNavigationToOnboarding = {})
    }
}