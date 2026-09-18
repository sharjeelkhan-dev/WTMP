package com.sharjeel.wtmp.ui.screens.stats

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharjeel.wtmp.ui.components.GlassCard
import com.sharjeel.wtmp.ui.theme.SuccessEmerald

// =================================================================
// 1. STATS SCREEN MAIN COMPOSABLE
// =================================================================

/**
 * StatsScreen displays security overview, protected session counts, intrusion stats,
 * and an animated activity chart using custom Canvas drawing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Security Overview",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Key Metrics Summary Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Protected Sessions",
                            value = uiState.protectedSessions.toString(),
                            color = colorScheme.primary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Intrusions Prevented",
                            value = uiState.intrusionsPrevented.toString(),
                            color = colorScheme.error
                        )
                    }
                }

                // Session Duration Metric
                item {
                    StatCard(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Avg. Session Length",
                        value = uiState.avgSessionLength,
                        color = colorScheme.secondary
                    )
                }

                // Real-time Canvas Activity Line Chart
                item {
                    ActivityChartCard(data = uiState.activityData)
                }

                // Security Score & Trend Progress Indicator
                item {
                    SecurityScoreTrendCard(
                        score = uiState.securityScore,
                        trend = uiState.scoreTrend
                    )
                }
            }
        }
    }
}

// =================================================================
// 2. REUSABLE STAT CARD COMPOSABLE
// =================================================================

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

// =================================================================
// 3. ANIMATED ACTIVITY CANVAS CHART
// =================================================================

@Composable
private fun ActivityChartCard(data: List<Float>) {
    val primaryColor = MaterialTheme.colorScheme.primary

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Activity Chart",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Infinite Progress Sweep for Dynamic Graph Drawing
            val animationProgress = rememberInfiniteTransition(label = "chart_transition").animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "chart_progress"
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val path = Path()
                val points = data.ifEmpty { listOf(0f) }
                val width = size.width
                val height = size.height
                val stepX = width / (points.size - 1).coerceAtLeast(1)

                points.forEachIndexed { index, point ->
                    val x = index * stepX
                    val y = height * (1 - point)
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevY = height * (1 - points[index - 1])

                        val segmentStart = index.toFloat() / points.size
                        val segmentEnd = (index + 1).toFloat() / points.size

                        if (animationProgress.value > segmentStart) {
                            val progress = ((animationProgress.value - segmentStart) / (segmentEnd - segmentStart)).coerceIn(0f, 1f)
                            val interpX = prevX + (x - prevX) * progress
                            val interpY = prevY + (y - prevY) * progress
                            path.lineTo(interpX, interpY)
                        }
                    }
                }

                // Core Line Stroke
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Glowing Outer Aura Stroke
                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.3f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

// =================================================================
// 4. SECURITY SCORE TREND CARD
// =================================================================

@Composable
private fun SecurityScoreTrendCard(
    score: Int,
    trend: String
) {
    val successColor = SuccessEmerald

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Security Score Trend",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$score/100",
                        style = MaterialTheme.typography.titleLarge,
                        color = successColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Last 7 Days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.titleMedium,
                        color = successColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { score.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = successColor,
                trackColor = successColor.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}