package com.sharjeel.wtmp.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.model.AiEventAnalysis
import com.sharjeel.wtmp.model.AppUsageInfo
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val eventState by viewModel.event.collectAsStateWithLifecycle()
    val aiAnalysis by viewModel.aiAnalysis.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    EventDetailsContent(
        event = eventState,
        aiAnalysis = aiAnalysis,
        isAiLoading = isAiLoading,
        onNavigateBack = onNavigateBack,
        onDeleteEvent = { event -> viewModel.deleteEvent(event) },
        onAnalyzeEvent = { viewModel.analyzeEvent() },
        onNavigateToPrevious = { viewModel.navigateToPrevious() },
        onNavigateToNext = { viewModel.navigateToNext() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsContent(
    event: SecurityEvent?,
    aiAnalysis: AiEventAnalysis?,
    isAiLoading: Boolean,
    onNavigateBack: () -> Unit,
    onDeleteEvent: (SecurityEvent) -> Unit,
    onAnalyzeEvent: () -> Unit,
    onNavigateToPrevious: () -> Unit,
    onNavigateToNext: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm:ss a", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    val scrollState = rememberScrollState()
    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text("Report Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.recycle_bin_icon),
                            contentDescription = "Delete",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (event != null) {
                Spacer(modifier = Modifier.height(8.dp))
                EvidenceCard(event.evidencePath)
                Spacer(modifier = Modifier.height(16.dp))
                AiForensicsCard(
                    analysis = aiAnalysis,
                    isLoading = isAiLoading,
                    onAnalyze = onAnalyzeEvent
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Information Card
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        InfoRow(Icons.Default.Schedule, "Time", dateFormat.format(Date(event.timestamp)), MaterialTheme.colorScheme.primary)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        InfoRow(Icons.Default.Security, "Trigger", event.type.title, MaterialTheme.colorScheme.secondary, badgeText = if (event.deviceState == "Unlock Failed") "Failed" else "Unlocked")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (event.accessedApps.isNotEmpty()) {
                    AppsOpenedCard(event.accessedApps, timeFormat)
                }

                Spacer(modifier = Modifier.height(24.dp))

                PaginationControls(
                    onPrevious = onNavigateToPrevious,
                    onNext = onNavigateToNext
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showDeleteDialog && event != null) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDeleteEvent(event)
                showDeleteDialog = false
                onNavigateBack()
            }
        )
    }
}

@Composable
fun EvidenceCard(path: String?) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!path.isNullOrEmpty()) {
                AsyncImage(
                    model = File(path),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(id = R.drawable.no_image_photography_icon), contentDescription = null, modifier = Modifier.size(48.dp))
                    Text("No Photo Evidence", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun AiForensicsCard(
    analysis: AiEventAnalysis?,
    isLoading: Boolean,
    onAnalyze: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF5F3FF)
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6).copy(alpha = 0.4f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color(0xFF8B5CF6).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("AI Visual Forensics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = textColor)
                Spacer(modifier = Modifier.weight(1f))
                if (analysis != null && !isLoading) {
                    TextButton(onClick = onAnalyze, contentPadding = PaddingValues(0.dp)) {
                        Text("Re-analyze", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6))
                    }
                }
            }

            AnimatedVisibility(visible = analysis != null || isLoading) {
                if (isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape), color = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Gemini is analyzing intruder features...", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                    }
                } else if (analysis != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            Text("Subject Identification:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = textColor)
                        }

                        Text(
                            text = analysis.intruderDescription ?: "No clear person detected.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = textColor.copy(alpha = 0.05f))

                        Text(
                            text = "Security Analysis: ${analysis.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (analysis == null && !isLoading) {
                Button(
                    onClick = onAnalyze,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Identify Intruder with AI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color, badgeText: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        if (badgeText != null) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                Text(badgeText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AppsOpenedCard(apps: List<AppUsageInfo>, timeFormat: SimpleDateFormat) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Apps Accessed", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            apps.forEachIndexed { index, app ->
                AppUsageItem(app, timeFormat)
                if (index < apps.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun PaginationControls(onPrevious: () -> Unit, onNext: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
            Text("Reports", style = MaterialTheme.typography.labelLarge)
            IconButton(onClick = onNext) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
        }
    }
}

@Composable
fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Log?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AppUsageItem(app: AppUsageInfo, timeFormat: SimpleDateFormat) {
    val context = LocalContext.current
    val appIcon = remember(app.packageName) {
        try { context.packageManager.getApplicationIcon(app.packageName).toBitmap().asImageBitmap() } catch (_: Exception) { null }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (appIcon != null) {
            Image(bitmap = appIcon, contentDescription = null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)))
        } else {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(app.appName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Text(timeFormat.format(Date(app.launchedTimestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun EventDetailsScreenPreview() {
    WTMPTheme {
        EventDetailsContent(
            event = SecurityEvent(
                type = SecurityEventType.DEVICE_UNLOCKED,
                timestamp = System.currentTimeMillis(),
                deviceState = "Unlocked",
                accessedApps = listOf(
                    AppUsageInfo("com.whatsapp", "WhatsApp", launchedTimestamp = System.currentTimeMillis() - 10000),
                    AppUsageInfo("com.instagram.android", "Instagram", launchedTimestamp = System.currentTimeMillis() - 5000)
                )
            ),
            aiAnalysis = AiEventAnalysis(
                explanation = "A routine device unlock event was detected. The user accessed communication and social media apps immediately after.",
                riskLevel = "Low",
                category = "Normal",
                recommendation = "No action required.",
                intruderDescription = "A male subject, age 20-25, wearing a black t-shirt was identified in the captured frame."
            ),
            isAiLoading = false,
            onNavigateBack = {},
            onDeleteEvent = {},
            onAnalyzeEvent = {},
            onNavigateToPrevious = {},
            onNavigateToNext = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EventDetailsScreenDarkPreview() {
    WTMPTheme {
        EventDetailsContent(
            event = SecurityEvent(
                type = SecurityEventType.FAILED_UNLOCK,
                timestamp = System.currentTimeMillis(),
                deviceState = "Unlock Failed",
                accessedApps = emptyList()
            ),
            aiAnalysis = null,
            isAiLoading = true,
            onNavigateBack = {},
            onDeleteEvent = {},
            onAnalyzeEvent = {},
            onNavigateToPrevious = {},
            onNavigateToNext = {}
        )
    }
}
