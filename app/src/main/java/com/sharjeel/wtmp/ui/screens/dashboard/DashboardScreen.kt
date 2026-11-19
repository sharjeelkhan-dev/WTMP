package com.sharjeel.wtmp.ui.screens.dashboard

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.model.AiSecurityReport
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.service.AdminReceiver
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import com.sharjeel.wtmp.utils.PermissionUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val EmeraldPrimary = Color(0xFF047857)
private val SwitchActiveGreen = Color(0xFF22C55E)
private val AlertRed = Color(0xFFEF4444)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToEventDetails: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
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
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToStats = onNavigateToStats
    )
}

sealed class NavigationIcon {
    data class Vector(val imageVector: ImageVector) : NavigationIcon()
    data class Drawable(val resId: Int) : NavigationIcon()
}

data class NavDockItem(
    val route: String,
    val title: String,
    val icon: NavigationIcon,
    val badgeCount: Int? = null
)

val DockItems = listOf(
    NavDockItem("dashboard", "Home", NavigationIcon.Drawable(R.drawable.home_icon_silhouette_svgrepo_com)),
    NavDockItem("history", "Activity", NavigationIcon.Vector(Icons.AutoMirrored.Filled.List), badgeCount = 2),
    NavDockItem("stats", "Photos", NavigationIcon.Vector(Icons.Default.Collections)),
    NavDockItem("settings", "Settings", NavigationIcon.Drawable(R.drawable.setting_icon))
)

@Composable
fun DockedBottomNavBar(
    currentRoute: String,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val navBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val dividerColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(navBg)
    ) {
        HorizontalDivider(color = dividerColor, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItems.forEach { item ->
                val isSelected = currentRoute.startsWith(item.route)
                val itemTint = if (isSelected) EmeraldPrimary else Color(0xFF94A3B8)

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRouteSelected(item.route) }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box {
                        when (val navIcon = item.icon) {
                            is NavigationIcon.Vector -> {
                                Icon(
                                    imageVector = navIcon.imageVector,
                                    contentDescription = item.title,
                                    tint = itemTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            is NavigationIcon.Drawable -> {
                                Icon(
                                    painter = painterResource(id = navIcon.resId),
                                    contentDescription = item.title,
                                    tint = itemTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (item.badgeCount != null && item.badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-3).dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(AlertRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${item.badgeCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = itemTint
                    )
                }
            }
        }
    }
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
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White

    var capturePhotoToggle by remember { mutableStateOf(true) }
    var alarmSoundToggle by remember { mutableStateOf(true) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var selectedDateFilter by remember { mutableStateOf("All") }
    var successfulUnlockChecked by remember { mutableStateOf(true) }
    var unsuccessfulUnlockChecked by remember { mutableStateOf(true) }
    var appLaunchedChecked by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            onToggleProtection()
        } else {
            Toast.makeText(context, "Permissions required for protection", Toast.LENGTH_LONG).show()
        }
    }

    val formattedDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = screenBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WTMP",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = EmeraldPrimary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { showAiDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Insights",
                                    modifier = Modifier.size(22.dp),
                                    tint = EmeraldPrimary
                                )
                            }

                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.filter_filtering_icon),
                                    contentDescription = "Filter",
                                    modifier = Modifier.size(22.dp),
                                    tint = EmeraldPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }

            item {
                ReferenceProtectionCard(
                    cardBg = cardBg,
                    isActive = uiState.isProtectionActive,
                    failedAttempts = uiState.events.count { it.type == SecurityEventType.FAILED_UNLOCK },
                    unlocksCount = uiState.events.count { it.type == SecurityEventType.DEVICE_UNLOCKED },
                    onToggle = {
                        if (!uiState.isProtectionActive) {
                            if (PermissionUtils.hasAllPermissions(context) && PermissionUtils.isAdminActive(context)) {
                                onToggleProtection()
                            } else {
                                if (!PermissionUtils.isAdminActive(context)) {
                                    Toast.makeText(context, "Activate Device Admin", Toast.LENGTH_LONG).show()
                                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(context, AdminReceiver::class.java))
                                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to monitor failed unlock attempts.")
                                    }
                                    context.startActivity(intent)
                                } else if (!PermissionUtils.hasUsageStatsPermission(context)) {
                                    Toast.makeText(context, "Grant Usage Access", Toast.LENGTH_LONG).show()
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

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT ACTIVITY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = EmeraldPrimary,
                            modifier = Modifier.clickable(onClick = onNavigateToHistory)
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            val sampleEvents = uiState.events.take(4)
                            if (sampleEvents.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recent activity recorded",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            } else {
                                sampleEvents.forEachIndexed { index, event ->
                                    ReferenceActivityRow(
                                        event = event,
                                        onClick = { onNavigateToEventDetails(event.id) }
                                    )
                                    if (index < sampleEvents.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 70.dp, end = 16.dp),
                                            color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "DETECTION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg
                    ) {
                        val rowDividerColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            DetectionSwitchRow(
                                icon = Icons.Default.CameraAlt,
                                iconBg = Color(0xFF007AFF),
                                title = "Capture intruder photo",
                                checked = capturePhotoToggle,
                                onCheckedChange = { capturePhotoToggle = it }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                                color = rowDividerColor,
                                thickness = 1.dp
                            )
                            DetectionValueRow(
                                icon = Icons.Default.Tag,
                                iconBg = Color(0xFFFF9500),
                                title = "Wrong attempts before capture",
                                value = "1",
                                onClick = onNavigateToSettings
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                                color = rowDividerColor,
                                thickness = 1.dp
                            )
                            DetectionSwitchRow(
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                iconBg = Color(0xFFDC2626),
                                title = "Alarm sound",
                                checked = alarmSoundToggle,
                                onCheckedChange = { alarmSoundToggle = it }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                                color = rowDividerColor,
                                thickness = 1.dp
                            )
                            DetectionValueRow(
                                icon = Icons.Default.Email,
                                iconBg = Color(0xFF5856D6),
                                title = "Email alert",
                                value = "Instant",
                                onClick = onNavigateToSettings
                            )
                        }
                    }

                    Text(
                        text = "Photos are stored on this device and backed up to Google Drive. Only you can view them.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        if (showAiDialog) {
            AiSecurityDialog(
                report = uiState.customReport,
                isLoading = uiState.isAiLoading,
                onSendPrompt = { prompt ->
                    onGenerateCustomReport(prompt)
                },
                onDismiss = { showAiDialog = false }
            )
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filter Reports",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    Text(
                        text = "Filter by Date / Time",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )

                    val dateOptions = listOf("Last 24 Hours", "Past Week", "Past Month", "Today", "All")
                    dateOptions.forEach { option ->
                        val isSelected = selectedDateFilter == option

                        val optionModifier = if (isSelected) {
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF131D2E))
                                .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { selectedDateFilter = option }
                                .padding(horizontal = 2.dp, vertical = 1.dp)
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedDateFilter = option }
                                .padding(vertical = 1.dp)
                        }

                        Box(modifier = optionModifier) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedDateFilter = option },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF22C55E),
                                        unselectedColor = Color(0xFF64748B)
                                    ),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Filter by Event Type",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { successfulUnlockChecked = !successfulUnlockChecked }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Successful Unlock",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = Color.White
                        )
                        Checkbox(
                            checked = successfulUnlockChecked,
                            onCheckedChange = { successfulUnlockChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF22C55E),
                                uncheckedColor = Color(0xFF64748B),
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { unsuccessfulUnlockChecked = !unsuccessfulUnlockChecked }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Unsuccessful Unlock",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = Color.White
                        )
                        Checkbox(
                            checked = unsuccessfulUnlockChecked,
                            onCheckedChange = { unsuccessfulUnlockChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF22C55E),
                                uncheckedColor = Color(0xFF64748B),
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { appLaunchedChecked = !appLaunchedChecked }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "App Launched",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = Color.White
                        )
                        Checkbox(
                            checked = appLaunchedChecked,
                            onCheckedChange = { appLaunchedChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF22C55E),
                                uncheckedColor = Color(0xFF64748B),
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedDateFilter = "All"
                                successfulUnlockChecked = true
                                unsuccessfulUnlockChecked = true
                                appLaunchedChecked = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B)
                            )
                        ) {
                            Text(
                                text = "Reset",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showFilterSheet = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E)
                            )
                        ) {
                            Text(
                                text = "Apply Filter",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ReferenceProtectionCard(
    cardBg: Color,
    isActive: Boolean,
    failedAttempts: Int,
    unlocksCount: Int,
    onToggle: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else Color(0xFF0F172A)
    val protectionBg = if (isActive) EmeraldPrimary else Color(0xFF64748B)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardBg
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(protectionBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.webcam_icon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isActive) "Protection is on" else "Protection is off",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = primaryText
                        )
                        Text(
                            text = "Monitoring since 08:12",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SwitchActiveGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                StatCol(
                    value = "$failedAttempts",
                    label = "Failed attempts",
                    valueColor = AlertRed
                )
                StatCol(
                    value = "$unlocksCount",
                    label = "Unlocks",
                    valueColor = primaryText
                )
                StatCol(
                    value = "10:44",
                    label = "Last backup",
                    valueColor = primaryText
                )
            }
        }
    }
}

@Composable
fun StatCol(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = valueColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color(0xFF64748B)
        )
    }
}

sealed class ActivityIcon {
    data class Vector(val imageVector: ImageVector) : ActivityIcon()
    data class Drawable(val resId: Int) : ActivityIcon()
}

@Composable
fun ReferenceActivityRow(
    event: SecurityEvent,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isUnlockSuccess = event.type == SecurityEventType.DEVICE_UNLOCKED || event.type == SecurityEventType.UNEXPECTED_UNLOCK
    val displayTitle = if (isUnlockSuccess) "Device unlocked" else "Failed unlock"
    val timeLabel = formatEventTime(event.timestamp)

    val (iconBg, iconTint, activityIcon) = when {
        !isUnlockSuccess && (event.deviceState.contains("pattern", ignoreCase = true) || event.deviceState.contains("camera", ignoreCase = true)) -> {
            Triple(Color(0xFFF1F5F9), Color(0xFF64748B), ActivityIcon.Drawable(R.drawable.no_image_photography_icon))
        }
        !isUnlockSuccess -> {
            Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), ActivityIcon.Drawable(R.drawable.cross_icon))
        }
        event.deviceState.contains("face", ignoreCase = true) -> {
            Triple(Color(0xFFF1F5F9), Color(0xFF64748B), ActivityIcon.Drawable(R.drawable.unlock_icon))
        }
        else -> {
            Triple(Color(0xFFD1FAE5), Color(0xFF059669), ActivityIcon.Vector(Icons.Default.Fingerprint))
        }
    }

    val subtitleText = event.deviceState.ifBlank {
        if (isUnlockSuccess) "Fingerprint • 2 photos • Office" else "Wrong PIN • 2 photos • Home"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) iconBg.copy(alpha = 0.18f) else iconBg),
            contentAlignment = Alignment.Center
        ) {
            when (activityIcon) {
                is ActivityIcon.Vector -> {
                    Icon(
                        imageVector = activityIcon.imageVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                is ActivityIcon.Drawable -> {
                    Icon(
                        painter = painterResource(id = activityIcon.resId),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun formatEventTime(timestamp: Long): String {
    val eventCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCal = Calendar.getInstance()
    val isSameDay = eventCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            eventCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    return if (isSameDay) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } else {
        "Yesterday"
    }
}

@Composable
fun DetectionSwitchRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SwitchActiveGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun DetectionValueRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = Color(0xFF64748B)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AiSecurityDialogContent(
    report: AiSecurityReport?,
    isLoading: Boolean,
    onSendPrompt: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var userPrompt by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 620.dp) // Constrained max height to prevent overflow
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = dialogBg,
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState), // Makes the entire content scrollable smoothly
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Security Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = textColor
                    )
                    Text(
                        text = "Smart logs analysis",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = mutedColor
                    )
                }
            }

            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = { Text("Ask AI (e.g. Check for failed logins)", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwitchActiveGreen,
                    unfocusedBorderColor = borderColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val quickPrompts = listOf("Risk Audit", "Night Logins", "Summary")
                quickPrompts.forEach { chipText ->
                    Surface(
                        onClick = {
                            val promptText = "Perform $chipText of my security events"
                            userPrompt = promptText
                            onSendPrompt(promptText)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF162032) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF23324A) else Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chipText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = textColor,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = borderColor, thickness = 0.8.dp)

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(color = SwitchActiveGreen, modifier = Modifier.size(24.dp))
                        Text(
                            "Analyzing security logs...",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = mutedColor
                        )
                    }
                }
            } else if (report != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF162032) else Color(0xFFF8FAFC))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = report.reportTitle.ifBlank { "Security Analysis" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = textColor
                        )
                        Surface(
                            color = SwitchActiveGreen,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${report.securityScore}%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = report.summary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = textColor.copy(alpha = 0.85f)
                    )

                    if (report.detailedInsights.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        report.detailedInsights.forEach { insight ->
                            Text(
                                text = "• $insight",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                color = mutedColor
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Tap a prompt chip above or type your question.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = mutedColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

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
                    border = BorderStroke(1.dp, borderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textColor,
                        containerColor = if (isDark) Color(0xFF162032) else Color.Transparent
                    )
                ) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )
                }

                Button(
                    onClick = {
                        val prompt = userPrompt.ifBlank { "Analyze overall security status and intrusion attempts" }
                        onSendPrompt(prompt)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SwitchActiveGreen
                    )
                ) {
                    Text(
                        text = "Ask AI",
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
fun AiSecurityDialog(
    report: AiSecurityReport?,
    isLoading: Boolean,
    onSendPrompt: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AiSecurityDialogContent(
            report = report,
            isLoading = isLoading,
            onSendPrompt = onSendPrompt,
            onDismiss = onDismiss
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardScreenPreview() {
    val now = System.currentTimeMillis()

    WTMPTheme {
        DashboardScreenContent(
            uiState = DashboardUiState(
                isProtectionActive = true,
                currentDate = "Saturday, 18 July",
                events = listOf(
                    SecurityEvent(
                        type = SecurityEventType.FAILED_UNLOCK,
                        timestamp = now,
                        deviceState = "Wrong PIN • 2 photos • Home"
                    )
                )
            ),
            onToggleProtection = {},
            onTimeIntervalSelected = {},
            onReportTypeToggled = {},
            onResetFilters = {},
            onGenerateCustomReport = {},
            onNavigateToEventDetails = {},
            onNavigateToSettings = {},
            onNavigateToHistory = {},
            onNavigateToStats = {}
        )
    }
}

@Preview(showBackground = true, name = "AI Pop-up - Light Mode")
@Preview(showBackground = true, name = "AI Pop-up - Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AiSecurityDialogPreview() {
    WTMPTheme {
        AiSecurityDialogContent(
            report = AiSecurityReport(
                reportTitle = "Overall Security Audit",
                securityScore = 95,
                summary = "Device protection is active with 0 unauthorized intrusion attempts detected today. Camera & Device Admin permissions are fully configured.",
                detailedInsights = listOf(
                    "🛡️ Protection Shield: Active (Monitoring since 08:12)",
                    "📷 Intruder Capture: On (Auto-photo after 1 attempt)",
                    "☁️ Cloud Backup: Google Drive Sync ready"
                )
            ),
            isLoading = false,
            onSendPrompt = {},
            onDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentInterval: TimeInterval,
    selectedTypes: Set<ReportType>,
    onIntervalSelected: (TimeInterval) -> Unit,
    onTypeToggled: (ReportType) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val sheetBg = if (isDark) Color(0xFF0D1527) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(mutedColor.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Filter Reports",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = textColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Filter by Date / Time",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = mutedColor
            )

            val intervals = listOf(
                TimeInterval.LAST_24_HOURS to "Last 24 Hours",
                TimeInterval.PAST_WEEK to "Past Week",
                TimeInterval.PAST_MONTH to "Past Month",
                TimeInterval.TODAY to "Today",
                TimeInterval.ALL to "All"
            )

            intervals.forEach { (interval, label) ->
                val isSelected = currentInterval == interval
                val rowBg = if (isSelected) {
                    if (isDark) Color(0xFF132B29) else Color(0xFFD1FAE5).copy(alpha = 0.5f)
                } else {
                    Color.Transparent
                }
                val borderColor = if (isSelected) SwitchActiveGreen else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(rowBg)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .clickable { onIntervalSelected(interval) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onIntervalSelected(interval) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = SwitchActiveGreen,
                            unselectedColor = mutedColor
                        )
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Filter by Event Type",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = mutedColor
            )

            val eventTypes = listOf(
                ReportType.SUCCESSFUL_UNLOCK to "Successful Unlock",
                ReportType.UNSUCCESSFUL_UNLOCK to "Unsuccessful Unlock",
                ReportType.APP_LAUNCHED to "App Launched"
            )

            eventTypes.forEach { (type, label) ->
                val isChecked = selectedTypes.contains(type)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTypeToggled(type) }
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = textColor
                    )

                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onTypeToggled(type) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SwitchActiveGreen,
                            uncheckedColor = mutedColor,
                            checkmarkColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onResetFilters,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF23324A) else Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textColor,
                        containerColor = if (isDark) Color(0xFF162032) else Color.Transparent
                    )
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SwitchActiveGreen
                    )
                ) {
                    Text(
                        text = "Apply Filter",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "Filter Sheet - Light Mode")
@Preview(showBackground = true, name = "Filter Sheet - Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FilterBottomSheetPreview() {
    WTMPTheme {
        FilterBottomSheet(
            currentInterval = TimeInterval.ALL,
            selectedTypes = setOf(ReportType.SUCCESSFUL_UNLOCK, ReportType.UNSUCCESSFUL_UNLOCK),
            onIntervalSelected = {},
            onTypeToggled = {},
            onResetFilters = {},
            onDismiss = {}
        )
    }
}