package com.sharjeel.wtmp.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.ui.components.BentoCard
import com.sharjeel.wtmp.model.AiEventAnalysis
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val EmeraldPrimary = Color(0xFF047857)
private val EmeraldAction = Color(0xFF0D7A5F)
private val SwitchActiveGreen = Color(0xFF22C55E)
private val AlertRed = Color(0xFFEF4444)
private val DestructiveRed = Color(0xFFDC2626)

@Composable
private fun DashboardCardSurface(
    modifier: Modifier = Modifier,
    cardBg: Color,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        content = content
    )
}

// =================================================================
// 1. MAIN SCREEN ENTRY POINT (Stateful Composable)
// =================================================================

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
        onAnalyzeEvent = { viewModel.analyzeEvent() }
    )
}

// =================================================================
// 2. MAIN CONTENT SCREEN (Stateless Composable)
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsContent(
    event: SecurityEvent?,
    aiAnalysis: AiEventAnalysis?,
    isAiLoading: Boolean,
    onNavigateBack: () -> Unit,
    onDeleteEvent: (SecurityEvent) -> Unit,
    onAnalyzeEvent: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showKnownPersonDialog by remember { mutableStateOf(false) }
    var showSavePhotosDialog by remember { mutableStateOf(false) }
    var showAiResultDialog by remember { mutableStateOf(false) }
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }

    val isDark = isSystemInDarkTheme()
    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val dividerColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM '·' HH:mm:ss", Locale.getDefault()) }
    val timeLabelFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val scrollState = rememberScrollState()

    val triggerAiAnalysis = {
        showAiResultDialog = true
        onAnalyzeEvent()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp),
                title = {
                    Text(
                        text = "Report",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onNavigateBack)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Activity",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Activity",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = { /* Share */ }) {
                            Icon(
                                imageVector = Icons.Outlined.IosShare,
                                contentDescription = "Share",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            EventMoreOptionsMenu(
                                expanded = showMoreMenu,
                                onAnalyze = triggerAiAnalysis,
                                onSavePhotos = { showSavePhotosDialog = true },
                                onRequestDelete = { showDeleteDialog = true },
                                onDismiss = { showMoreMenu = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBg
                )
            )
        },
        containerColor = screenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp + navBarPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (event != null) {
                val isUnlockSuccess =
                    event.type == SecurityEventType.DEVICE_UNLOCKED ||
                            event.type == SecurityEventType.UNEXPECTED_UNLOCK
                val statusText = if (isUnlockSuccess) "Device unlocked" else "Failed unlock"
                val mainTitleText =
                    if (isUnlockSuccess) "Device unlocked successfully" else "Wrong PIN entered 3 times"
                val statusColor = if (isUnlockSuccess) EmeraldPrimary else AlertRed

                ReportEvidenceSection(
                    path = event.evidencePath,
                    timestampText = timeLabelFormat.format(Date(event.timestamp)),
                    selectedIndex = selectedPhotoIndex,
                    totalPhotos = 2,
                    onSelectPhoto = { index -> selectedPhotoIndex = index },
                    isDark = isDark
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = statusColor
                        )
                    }

                    Text(
                        text = mainTitleText,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 23.sp,
                            lineHeight = 28.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Text(
                        text = dateFormat.format(Date(event.timestamp)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    DashboardCardSurface(cardBg = cardBg) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            ReportDetailRow(
                                label = "Method",
                                value = if (isUnlockSuccess) "Fingerprint" else "PIN",
                                valueColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = dividerColor,
                                thickness = 1.dp
                            )
                            ReportDetailRow(
                                label = "Attempts",
                                value = if (isUnlockSuccess) "1 try" else "3 wrong",
                                valueColor = if (isUnlockSuccess) EmeraldPrimary else AlertRed,
                                isBoldValue = true
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = dividerColor,
                                thickness = 1.dp
                            )
                            ReportDetailRow(
                                label = "Camera",
                                value = "Front · 2 photos",
                                valueColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = dividerColor,
                                thickness = 1.dp
                            )
                            ReportDetailRow(
                                label = "Device state",
                                value = "Locked · 64% battery",
                                valueColor = Color(0xFF64748B)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = dividerColor,
                                thickness = 1.dp
                            )
                            ReportDetailRow(
                                label = "Backup",
                                value = "✓ Google Drive",
                                valueColor = EmeraldPrimary
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "LOCATION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    DashboardCardSurface(cardBg = cardBg) {
                        Column {
                            LocationMapPreview(isDark = isDark)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Home",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        ),
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "12th Main Road, City Sector",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "Open in Maps",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = EmeraldPrimary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { /* Open Maps */ }
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                DashboardCardSurface(cardBg = cardBg) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        QuickActionRow(
                            icon = Icons.Default.PersonAdd,
                            iconBg = EmeraldPrimary,
                            title = "Mark as known person",
                            onClick = { showKnownPersonDialog = true }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                            color = dividerColor,
                            thickness = 1.dp
                        )
                        QuickActionRow(
                            icon = Icons.Default.Download,
                            iconBg = Color(0xFF007AFF),
                            title = "Save photos to gallery",
                            onClick = { showSavePhotosDialog = true }
                        )
                    }
                }

                DashboardCardSurface(
                    cardBg = cardBg,
                    onClick = { showDeleteDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delete Report",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = AlertRed
                        )
                    }
                }

                Text(
                    text = "Report ID WT-20260718-1042 · Recorded by WTMP 3.2.1",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    color = Color(0xFF64748B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            }
        }
    }

    if (showAiResultDialog) {
        AiAnalysisResultDialog(
            analysis = aiAnalysis,
            isLoading = isAiLoading,
            onRetry = onAnalyzeEvent,
            onDismiss = { showAiResultDialog = false }
        )
    }

    if (showDeleteDialog && event != null) {
        DeleteConfirmDialog(
            photoCount = 2,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDeleteEvent(event)
                showDeleteDialog = false
                onNavigateBack()
            }
        )
    }

    if (showKnownPersonDialog) {
        MarkKnownPersonDialog(
            onDismiss = { showKnownPersonDialog = false },
            onConfirm = { showKnownPersonDialog = false }
        )
    }

    if (showSavePhotosDialog) {
        SavePhotosDialog(
            photoCount = 2,
            onDismiss = { showSavePhotosDialog = false },
            onConfirm = { showSavePhotosDialog = false }
        )
    }
}

// =================================================================
// 3. POP-UPS, DIALOGS & DROPDOWN MENU
// =================================================================

@Composable
fun AiAnalysisResultDialogContent(
    analysis: AiEventAnalysis?,
    isLoading: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val panelBg = if (isDark) Color(0xFF162032) else Color(0xFFF8FAFC)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = dialogBg,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldAction,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI Visual Forensics",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = "Intruder Identification & Analysis",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = EmeraldAction,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (analysis != null && !isLoading) {
                    Surface(
                        color = if (analysis.riskLevel.equals("High", ignoreCase = true)) DestructiveRed else EmeraldAction,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = analysis.category.ifBlank { "Normal" },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(panelBg)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = EmeraldAction,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "Analyzing intruder photo with Gemini AI...",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = mutedColor
                        )
                    }
                }
            } else if (analysis != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(panelBg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = EmeraldAction,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Subject Identification:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = textColor
                        )
                    }

                    Text(
                        text = analysis.intruderDescription?.ifBlank { null }
                            ?: "Subject captured in frame during credential verification. Front camera photo stored in secure local vault.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = textColor.copy(alpha = 0.9f)
                    )

                    HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), thickness = 0.8.dp)

                    Text(
                        text = "Security Analysis: ${analysis.explanation.ifBlank { "Your device was accessed and the front camera captured a photo of the person holding it." }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        color = mutedColor
                    )

                    if (analysis.recommendation.isNotBlank()) {
                        Text(
                            text = "Recommendation: ${analysis.recommendation}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = EmeraldAction
                        )
                    }
                }
            } else {
                Text(
                    text = "No analysis available yet. Tap Re-run to analyze this event.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = mutedColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = "Close",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onRetry,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldAction,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (analysis == null) "Run AI" else "Re-run",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AiAnalysisResultDialog(
    analysis: AiEventAnalysis?,
    isLoading: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AiAnalysisResultDialogContent(
            analysis = analysis,
            isLoading = isLoading,
            onRetry = onRetry,
            onDismiss = onDismiss
        )
    }
}

@Preview(showBackground = true, name = "AI Forensics Pop-up - Light Mode")
@Preview(showBackground = true, name = "AI Forensics Pop-up - Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AiAnalysisResultDialogPreview() {
    WTMPTheme {
        AiAnalysisResultDialogContent(
            analysis = AiEventAnalysis(
                explanation = "Your device was successfully unlocked on August 22, 2026, at 8:22 PM, and the front camera captured a photo of the person holding it. No unauthorized apps were accessed during this session.",
                riskLevel = "Low",
                category = "Normal",
                intruderDescription = "A young adult male with dark hair, thick and prominent dark eyebrows, and light facial stubble. He is wearing a blue, green, and white plaid collared shirt and has black wireless earbuds in his ears. The background consists of a dark brown curtain with an intricate gold floral pattern.",
                recommendation = "No security action required. Credentials verified."
            ),
            isLoading = false,
            onRetry = {},
            onDismiss = {}
        )
    }
}

@Composable
fun EventMoreOptionsMenu(
    expanded: Boolean,
    onAnalyze: () -> Unit,
    onSavePhotos: () -> Unit,
    onRequestDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val menuBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val dividerColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surface = menuBg,
            surfaceContainer = menuBg,
            surfaceContainerLow = menuBg,
            surfaceContainerHigh = menuBg,
            surfaceContainerHighest = menuBg
        ),
        shapes = MaterialTheme.shapes.copy(
            extraSmall = RoundedCornerShape(16.dp)
        )
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            offset = DpOffset(x = 0.dp, y = 4.dp),
            modifier = Modifier
                .width(210.dp)
                .background(menuBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(menuBg)
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onAnalyze()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = EmeraldAction,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Analyze with AI",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onSavePhotos()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = mutedColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Save photos",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = dividerColor,
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onRequestDelete()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = DestructiveRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Delete report",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = DestructiveRed,
                        modifier = Modifier.weight(1f)
                    )
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
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldAction.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldAction,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "AI Visual Forensics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = textColor
                    )
                }

                if (analysis != null && !isLoading) {
                    Text(
                        text = "Re-analyze",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = EmeraldAction,
                        modifier = Modifier.clickable(onClick = onAnalyze)
                    )
                }
            }

            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape),
                        color = EmeraldAction
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Analyzing intruder photo with Gemini AI...",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = mutedColor
                    )
                }
            } else if (analysis != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = EmeraldAction,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Subject Identification:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = textColor
                        )
                    }

                    Text(
                        text = analysis.intruderDescription?.ifBlank { null }
                            ?: "A young adult subject was captured in the frame during credential verification. Front camera photo evidence securely stored.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        ),
                        color = textColor.copy(alpha = 0.9f)
                    )

                    HorizontalDivider(
                        color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )

                    Text(
                        text = "Security Analysis: ${analysis.explanation.ifBlank { "Your device was accessed and the front camera captured a photo of the person holding it." }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        color = mutedColor
                    )
                }
            } else {
                Button(
                    onClick = onAnalyze,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAction)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyze Intruder with AI",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    photoCount: Int = 2,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val bodyColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val dividerColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = dialogBg,
            shadowElevation = 6.dp,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Delete this report?",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = titleColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "The $photoCount photos will also be removed from this phone and Google Drive. This can't be undone.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = bodyColor,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(onClick = onDismiss)
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = EmeraldAction
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = dividerColor,
                        thickness = 1.dp
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(onClick = onConfirm)
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = DestructiveRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarkKnownPersonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var personName by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = dialogBg,
            shadowElevation = 6.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Mark as known person",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = textColor
                )

                Text(
                    text = "Add a name so future attempts from this person are recognized.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = mutedColor
                )

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    placeholder = { Text("Enter name (e.g. Roommate)", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldAction,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            text = "Cancel",
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onConfirm(personName.trim()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldAction,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavePhotosDialog(
    photoCount: Int = 2,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = dialogBg,
            shadowElevation = 6.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Save photos to gallery?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = textColor
                )

                Text(
                    text = "Export $photoCount captured photos from this report to your device gallery.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = mutedColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            text = "Cancel",
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldAction,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// =================================================================
// 4. MODULAR SUB-COMPOSABLES
// =================================================================

@Composable
fun ReportEvidenceSection(
    path: String?,
    timestampText: String,
    selectedIndex: Int,
    totalPhotos: Int,
    onSelectPhoto: (Int) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.22f),
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF1E293B) else Color(0xFFD1D5DB)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!path.isNullOrEmpty()) {
                    AsyncImage(
                        model = File(path),
                        contentDescription = "Intruder photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.no_image_photography_icon),
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF4B5563),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedIndex == 0) "Photo 1" else "Photo 2",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF4B5563)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.56f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(selectedIndex + 1).coerceIn(1, totalPhotos)} / $totalPhotos",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.58f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Front camera · $timestampText",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalPhotos) { index ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) {
                                Modifier.border(2.dp, EmeraldPrimary, RoundedCornerShape(12.dp))
                            } else {
                                Modifier
                            }
                        )
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE5E7EB))
                        .clickable { onSelectPhoto(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (!path.isNullOrEmpty() && index == 0) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.no_image_photography_icon),
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationMapPreview(isDark: Boolean) {
    val mapBg = if (isDark) Color(0xFF162032) else Color(0xFFF3F4F6)
    val roadColor = if (isDark) Color(0xFF334155) else Color.White
    val greenBlock = if (isDark) Color(0xFF14532D).copy(alpha = 0.45f) else Color(0xFFDCEEE0)
    val blueBlock = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.45f) else Color(0xFFDBEAFE)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .background(mapBg)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 22.dp)
                .width(74.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(greenBlock)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 18.dp)
                .width(56.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(blueBlock)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(14.dp)
                .background(roadColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 104.dp)
                .width(14.dp)
                .fillMaxSize()
                .background(roadColor)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .clip(CircleShape)
                .background(AlertRed.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AlertRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.person_svgrepo_com),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ReportDetailRow(
    label: String,
    value: String,
    valueColor: Color,
    isBoldValue: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBoldValue) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 15.sp
            ),
            color = valueColor
        )
    }
}

@Composable
fun QuickActionRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            color = if (isDark) Color.White else Color(0xFF0F172A),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EventDetailsScreenPreview() {
    WTMPTheme {
        EventDetailsContent(
            event = SecurityEvent(
                type = SecurityEventType.FAILED_UNLOCK,
                timestamp = System.currentTimeMillis(),
                deviceState = "Wrong PIN entered 3 times"
            ),
            aiAnalysis = AiEventAnalysis(
                category = "Unauthorized Unlock Attempt",
                riskLevel = "Medium",
                explanation = "Three consecutive incorrect PIN entries occurred while the device was locked. Front camera captured 2 frames.",
                recommendation = "Review captured photos and enable stronger lock screen alerts."
            ),
            isAiLoading = false,
            onNavigateBack = {},
            onDeleteEvent = {},
            onAnalyzeEvent = {}
        )
    }
}