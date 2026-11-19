package com.sharjeel.wtmp.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.model.AiEventAnalysis
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val EmeraldPrimary = Color(0xFF047857)
private val SwitchActiveGreen = Color(0xFF22C55E)
private val AlertRed = Color(0xFFEF4444)

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
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }

    val isDark = isSystemInDarkTheme()
    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val dividerColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM '·' HH:mm:ss", Locale.getDefault()) }
    val timeLabelFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val scrollState = rememberScrollState()

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
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(22.dp)
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

                // 1. Photo Evidence Display with Overlay Badges & Thumbnails
                ReportEvidenceSection(
                    path = event.evidencePath,
                    timestampText = timeLabelFormat.format(Date(event.timestamp)),
                    selectedIndex = selectedPhotoIndex,
                    totalPhotos = 2,
                    onSelectPhoto = { index -> selectedPhotoIndex = index },
                    isDark = isDark
                )

                // 2. Status & Title Description
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

                // 3. DETAILS Grouped Card
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

                // 4. LOCATION Grouped Card
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
                                        text = "12th Main Rd, Indiranagar, Ben...",
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

                // 5. Quick Actions Grouped Card
                DashboardCardSurface(cardBg = cardBg) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        QuickActionRow(
                            icon = Icons.Default.PersonAdd,
                            iconBg = EmeraldPrimary,
                            title = "Mark as known person",
                            onClick = {}
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
                            onClick = {}
                        )
                    }
                }

                // 6. AI Forensics Card
                AiForensicsCard(
                    analysis = aiAnalysis,
                    isLoading = isAiLoading,
                    cardBg = cardBg,
                    onAnalyze = onAnalyzeEvent
                )

                // 7. Delete Report Action Card
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

                // 8. Footer Info Text
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

// =================================================================
// 3. MODULAR SUB-COMPOSABLES
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

                // Top Right Pill Overlay ("1 / 2")
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

                // Bottom Left Pill Overlay ("Front camera · 10:42:17")
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

        // Thumbnails Strip
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

@Composable
fun AiForensicsCard(
    analysis: AiEventAnalysis?,
    isLoading: Boolean,
    cardBg: Color,
    onAnalyze: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)

    DashboardCardSurface(cardBg = cardBg) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(9.dp),
                    color = EmeraldPrimary.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI Visual Forensics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape),
                    color = SwitchActiveGreen
                )
            } else if (analysis != null) {
                Text(
                    text = analysis.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.9f)
                )
            } else {
                Button(
                    onClick = onAnalyze,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SwitchActiveGreen)
                ) {
                    Text(
                        text = "Analyze Intruder with AI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Report?") },
        text = { Text("This security report and captured photos will be permanently deleted.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = AlertRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
            aiAnalysis = null,
            isAiLoading = false,
            onNavigateBack = {},
            onDeleteEvent = {},
            onAnalyzeEvent = {}
        )
    }
}