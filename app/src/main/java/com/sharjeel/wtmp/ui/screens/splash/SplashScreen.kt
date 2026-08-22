package com.sharjeel.wtmp.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

@Composable
fun SplashScreen(onNavigationToOnboarding: () -> Unit) {
    val currentOnNavigate by rememberUpdatedState(onNavigationToOnboarding)
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    // Screen Exit Animation States
    val screenAlpha = remember { Animatable(0f) }
    val screenScale = remember { Animatable(0.92f) }

    // Staggered Animations for Elements
    val iconScale = remember { Animatable(0.4f) }
    val iconAlpha = remember { Animatable(0f) }

    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(20f) }

    val subtitleAlpha = remember { Animatable(0f) }
    val subtitleOffsetY = remember { Animatable(15f) }

    // Branded Theme Palette
    val backgroundCol = colorScheme.background
    val cardBackground = colorScheme.surface
    val themePrimary = colorScheme.primary
    val textMain = colorScheme.onBackground
    val textSubtle = colorScheme.onBackground.copy(alpha = 0.7f)

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

    LaunchedEffect(Unit) {
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
        delay(350.milliseconds)
        launch { textAlpha.animateTo(1f, tween(400)) }
        launch { textOffsetY.animateTo(0f, tween(400, easing = LinearOutSlowInEasing)) }
        delay(150.milliseconds)
        launch { subtitleAlpha.animateTo(1f, tween(400)) }
        launch { subtitleOffsetY.animateTo(0f, tween(400, easing = LinearOutSlowInEasing)) }
        delay(1500.milliseconds)
        launch { screenAlpha.animateTo(0f, tween(350)) }
        launch { screenScale.animateTo(1.08f, tween(350, easing = FastOutSlowInEasing)) }
        delay(350.milliseconds)
        currentOnNavigate()
    }
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
            ) {
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