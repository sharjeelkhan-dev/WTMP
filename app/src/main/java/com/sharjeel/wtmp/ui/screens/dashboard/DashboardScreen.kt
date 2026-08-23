package com.sharjeel.wtmp.ui.screens.dashboard

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.model.AiSecurityReport
import com.sharjeel.wtmp.model.AppUsageInfo
import com.sharjeel.wtmp.model.EventSeverity
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.service.AdminReceiver
import com.sharjeel.wtmp.ui.theme.AlertRose
import com.sharjeel.wtmp.ui.theme.AvatarColors
import com.sharjeel.wtmp.ui.theme.SuccessEmerald
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import com.sharjeel.wtmp.utils.PermissionUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToEventDetails: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreenContent(
        uiState = uiState,
        onToggleProtection = viewModel::toggleProtection,
        onTimeIntervalSelected = viewModel::setTimeInterval,
        onReportTypeToggled = viewModel::toggleReportType,
        onResetFilters = viewModel::resetFilters,
        onGenerateCustomReport = viewModel::generateCustomAiReport,
        onNavigateToEventDetails = onNavigateToEventDetails,
        onNavigateToSettings = onNavigateToSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenContent(
    uiState: DashboardUiState,
    onToggleProtection: () -> Unit,
    onTimeIntervalSelected: (TimeInterval) -> Unit,
    onReportTypeToggled: (ReportType) -> Unit,
    onResetFilters: () -> Unit,
    onGenerateCustomReport: (String) -> Unit,
    onNavigateToEventDetails: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAiReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            onToggleProtection()
        } else {
            Toast.makeText(context, "Permissions required for protection", Toast.LENGTH_LONG).show()
        }
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp),
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.currentDate,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "WTMP",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showAiReportDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Report",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.filter_filtering_icon),
                                contentDescription = "Filter",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                painter = painterResource(id = R.drawable.setting_icon),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp + navBarPadding)
        ) {
            item {
                SecurityHeroCard(
                    isActive = uiState.isProtectionActive,
                    onToggle = {
                        if (!uiState.isProtectionActive) {
                            if (PermissionUtils.hasAllPermissions(context) && PermissionUtils.isAdminActive(context)) {
                                onToggleProtection()
                            } else {
                                if (!PermissionUtils.isAdminActive(context)) {
                                    Toast.makeText(context, "Activate Device Admin to detect failed unlocks", Toast.LENGTH_LONG).show()
                                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(context, AdminReceiver::class.java))
                                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to monitor failed unlock attempts.")
                                    }
                                    context.startActivity(intent)
                                } else if (!PermissionUtils.hasUsageStatsPermission(context)) {
                                    Toast.makeText(context, "Grant Usage Access to track apps", Toast.LENGTH_LONG).show()
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                } else {
                                    permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                                }
                            }
                        } else {
                            onToggleProtection()
                        }
                    }
                )
            }

            if (uiState.customReport != null) {
                item {
                    CustomAiReportCard(report = uiState.customReport)
                }
            } else if (uiState.isAiLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Security Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (uiState.events.isNotEmpty()) {
                        Text(
                            text = "${uiState.events.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (uiState.events.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyState()
                }
            } else {
                items(uiState.events) { event ->
                    EventItem(
                        event = event,
                        onClick = { onNavigateToEventDetails(event.id) }
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentInterval = uiState.timeInterval,
            selectedTypes = uiState.reportTypes,
            onIntervalSelected = onTimeIntervalSelected,
            onTypeToggled = onReportTypeToggled,
            onResetFilters = onResetFilters,
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showAiReportDialog) {
        AiPromptDialog(
            onDismiss = { showAiReportDialog = false },
            onConfirm = { prompt ->
                onGenerateCustomReport(prompt)
                showAiReportDialog = false
            }
        )
    }
}

@Composable
fun AiPromptDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var prompt by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF8B5CF6)
            )
        },
        title = {
            Text(
                "AI Security Audit",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Describe what you'd like the AI to investigate in your security history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("e.g. Check for night-time unlock attempts") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(prompt) },
                enabled = prompt.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Text("Generate Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CustomAiReportCard(report: AiSecurityReport) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6).copy(alpha = 0.4f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                    border = BorderStroke(0.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.reportTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = "AI Security Intelligence",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B5CF6),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Surface(
                    color = Color(0xFF8B5CF6),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "${report.securityScore}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.9f),
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (report.detailedInsights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = textColor.copy(alpha = 0.1f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                report.detailedInsights.forEach { insight ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.four_squares_icon),
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6).copy(alpha = 0.7f),
                            modifier = Modifier.size(8.dp).padding(top = 6.dp)
                        )
                        Text(
                            text = insight,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.75f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    WTMPTheme {
        DashboardScreenContent(
            uiState = DashboardUiState(
                isProtectionActive = true,
                currentDate = "SATURDAY, 18 JULY 2026",
                events = listOf(
                    SecurityEvent(
                        type = SecurityEventType.DEVICE_UNLOCKED,
                        timestamp = System.currentTimeMillis(),
                        deviceState = "Device Unlocked",
                        accessedApps = listOf(
                            AppUsageInfo("com.android.chrome", "Chrome", launchedTimestamp = System.currentTimeMillis()),
                            AppUsageInfo("com.google.android.youtube", "YouTube", launchedTimestamp = System.currentTimeMillis())
                        )
                    ),
                    SecurityEvent(
                        type = SecurityEventType.FAILED_UNLOCK,
                        timestamp = System.currentTimeMillis() - 3600000,
                        deviceState = "Unlock Failed",
                        severity = EventSeverity.HIGH
                    )
                )
            ),
            onToggleProtection = {},
            onTimeIntervalSelected = {},
            onReportTypeToggled = {},
            onResetFilters = {},
            onGenerateCustomReport = {},
            onNavigateToEventDetails = {},
            onNavigateToSettings = {}
        )
    }
}

@Composable
fun SecurityHeroCard(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val surfaceColor = colorScheme.surface
    val textColor = colorScheme.onSurface

    val activeGradient = if (isDark) {
        listOf(primaryColor.copy(alpha = 0.25f), Color(0xFF0F172A), Color(0xFF020617))
    } else {
        listOf(primaryColor.copy(alpha = 0.08f), surfaceColor, Color(0xFFF1F5F9))
    }

    val inactiveGradient = if (isDark) {
        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
    }

    val currentGradient = if (isActive) activeGradient else inactiveGradient

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(currentGradient))
                .border(
                    width = 1.dp,
                    color = if (isActive) SuccessEmerald.copy(alpha = 0.3f) else textColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfessionalSecurityHeader(isActive = isActive)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.08f))
                        .border(1.dp, textColor.copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) SuccessEmerald else AlertRose)
                    )
                    Text(
                        text = if (isActive) "SHIELD ON" else "SHIELD OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    onClick = onToggle,
                    shape = CircleShape,
                    color = if (isActive) SuccessEmerald else AlertRose,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.power_off_line_icon),
                            contentDescription = "Security Status",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalSecurityHeader(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarSpin")
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = if (isActive) SuccessEmerald else AlertRose

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle")

    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .drawBehind {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color.Transparent, statusColor)
                            ),
                            startAngle = angle,
                            sweepAngle = 120f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
            )
        }

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.15f))
                .border(
                    width = 1.5.dp,
                    color = statusColor.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.webcam_icon),
                contentDescription = "Security Status",
                tint = statusColor,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
fun EventItem(
    event: SecurityEvent,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val avatarColor = remember(event.id) { AvatarColors.random() }

    val isUnlockSuccess = event.type == SecurityEventType.DEVICE_UNLOCKED || event.type == SecurityEventType.UNEXPECTED_UNLOCK
    val displayTitle = when (event.type) {
        SecurityEventType.DEVICE_UNLOCKED -> "Device Unlocked"
        SecurityEventType.FAILED_UNLOCK -> "Failed Unlock Attempt"
        SecurityEventType.UNEXPECTED_UNLOCK -> "Device Unlocked"
        else -> event.type.title
    }

    val patternTint = if (isUnlockSuccess) SuccessEmerald else AlertRose
    val launchedAppsCount = event.accessedApps.size
    val isSynced = false

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(avatarColor, avatarColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (event.type) {
                    SecurityEventType.FAILED_UNLOCK, SecurityEventType.FAILED_ATTEMPT -> {
                        Icon(
                            painter = painterResource(id = R.drawable.cross_icon),
                            contentDescription = "Failed Attempt",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    SecurityEventType.DEVICE_UNLOCKED, SecurityEventType.UNEXPECTED_UNLOCK -> {
                        Icon(
                            painter = painterResource(id = R.drawable.unlock_icon),
                            contentDescription = "Device Unlocked",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(Date(event.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$launchedAppsCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "Launched Apps Count",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Pattern,
                    contentDescription = "Unlock Pattern Status",
                    tint = patternTint,
                    modifier = Modifier.size(18.dp)
                )
                Icon(
                    imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = if (isSynced) "Saved to Cloud" else "Not Synced to Cloud",
                    tint = if (isSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No reports yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Turn on protection to start monitoring your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentInterval: TimeInterval,
    selectedTypes: Set<ReportType>,
    onIntervalSelected: (TimeInterval) -> Unit,
    onTypeToggled: (ReportType) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val isDark = isSystemInDarkTheme()

    val sheetContainerColor = if (isDark) Color(0xFF090D16) else MaterialTheme.colorScheme.surface
    val contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = sheetContainerColor,
        tonalElevation = 16.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.25f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Filter by Date / Time",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TimeInterval.entries.forEach { interval ->
                            val isSelected = currentInterval == interval
                            val label = interval.name.lowercase().replace('_', ' ').split(' ')
                                .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF10B981).copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { onIntervalSelected(interval) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            color = if (isSelected) Color(0xFF10B981) else contentColor.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                    }
                                }

                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) contentColor else contentColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(contentColor.copy(alpha = 0.06f))
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Filter by Event Type",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor.copy(alpha = 0.6f)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ReportType.entries.forEach { type ->
                            val isSelected = type in selectedTypes
                            val label = when (type.name) {
                                "UNEXPECTED_UNLOCK" -> "Device Unlocked"
                                else -> type.name.lowercase().replace('_', ' ').split(' ')
                                    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onTypeToggled(type) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) contentColor else contentColor.copy(alpha = 0.7f),
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )

                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color(0xFF10B981) else contentColor.copy(alpha = 0.08f))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFF10B981) else contentColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = onResetFilters,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF131B2E) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, contentColor.copy(alpha = 0.15f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.85f)
                        )
                    }
                }

                Surface(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF10B981),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Apply Filter",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}