package com.sharjeel.wtmp.ui.screens.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharjeel.wtmp.ui.components.GlassCard
import com.sharjeel.wtmp.ui.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Security Overview",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = WtmpPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WtmpPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = WtmpPurpleBackground
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WtmpPrimary)
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
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Protected Sessions",
                            value = uiState.protectedSessions.toString(),
                            color = WtmpPrimary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Intrusions Prevented",
                            value = uiState.intrusionsPrevented.toString(),
                            color = WtmpRed
                        )
                    }
                }

                item {
                    StatCard(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Avg. Session Length",
                        value = uiState.avgSessionLength,
                        color = WtmpSecondary
                    )
                }

                item {
                    ActivityChartCard(data = uiState.activityData)
                }

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
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = WtmpTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun ActivityChartCard(data: List<Float>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Activity Chart",
                style = MaterialTheme.typography.titleMedium,
                color = WtmpTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            val animationProgress = rememberInfiniteTransition(label = "").animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ), label = ""
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val path = Path()
                val points = if (data.isEmpty()) listOf(0f) else data
                val width = size.width
                val height = size.height
                val stepX = width / (points.size - 1).coerceAtLeast(1)

                points.forEachIndexed { index, point ->
                    val x = index * stepX
                    val y = height * (1 - point)
                    if (index == 0) path.moveTo(x, y) else {
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

                drawPath(
                    path = path,
                    color = WtmpPrimary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                drawPath(
                    path = path,
                    color = WtmpPrimary.copy(alpha = 0.3f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

@Composable
private fun SecurityScoreTrendCard(
    score: Int,
    trend: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Security Score Trend",
                style = MaterialTheme.typography.titleMedium,
                color = WtmpTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Current Score", style = MaterialTheme.typography.labelSmall, color = WtmpTextSecondary)
                    Text(text = "$score/100", style = MaterialTheme.typography.titleLarge, color = WtmpGreen, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Last 7 Days", style = MaterialTheme.typography.labelSmall, color = WtmpTextSecondary)
                    Text(text = trend, style = MaterialTheme.typography.titleMedium, color = WtmpGreen, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { score.toFloat() / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = WtmpGreen,
                trackColor = WtmpGreen.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
