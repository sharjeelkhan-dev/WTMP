package com.sharjeel.wtmp.ui.screens.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.ui.components.BentoCard
import com.sharjeel.wtmp.ui.screens.dashboard.DockedBottomNavBar
import com.sharjeel.wtmp.ui.theme.WTMPTheme

private val EmeraldPrimary = Color(0xFF059669)
private val SwitchActiveGreen = Color(0xFF10B981)

// =================================================================
// 1. STATEFUL CONTAINER SCREEN
// =================================================================

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToStats = onNavigateToStats,
        onBiometricToggle = viewModel::updateBiometric,
        onAlarmToggle = viewModel::updateAlarm
    )
}

// =================================================================
// 2. STATELESS CONTENT COMPOSABLE
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState = SettingsUiState(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onBiometricToggle: (Boolean) -> Unit = {},
    onAlarmToggle: (Boolean) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var searchQuery by remember { mutableStateOf("") }

    // Toggle states for Settings controls
    var capturePhotoToggle by remember { mutableStateOf(true) }
    var captureOnUnlockToggle by remember { mutableStateOf(false) }
    var notificationsToggle by remember { mutableStateOf(true) }
    var alarmSoundToggle by remember { mutableStateOf(uiState.isAlarmEnabled) }
    var includeLocationToggle by remember { mutableStateOf(true) }
    var appLockToggle by remember { mutableStateOf(uiState.isBiometricEnabled) }
    var hideAppIconToggle by remember { mutableStateOf(false) }
    var fakeCrashToggle by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp),
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBg
                )
            )
        },
        containerColor = screenBg,
        bottomBar = {
            DockedBottomNavBar(
                currentRoute = "settings",
                activityBadgeCount = uiState.unreadActivityCount,
                onRouteSelected = { route ->
                    when (route) {
                        "dashboard" -> onNavigateBack()
                        "history" -> onNavigateToHistory()
                        "stats" -> onNavigateToStats()
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 100.dp + navBarPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Settings Header Title & Search Bar
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                color = Color(0xFF94A3B8)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.magnifying_glass_icon),
                                contentDescription = null,
                                tint =  MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SwitchActiveGreen,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg
                        ),
                        singleLine = true
                    )
                }
            }
            // 2. User Profile Card
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Aarav Mehta",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )

                                    Surface(
                                        color = EmeraldPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp
                                            ),
                                            color = EmeraldPrimary
                                        )
                                    }
                                }

                                Text(
                                    text = "aarav.mehta@gmail.com • Renews...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 3. PROTECTION Grouped Bento Card
            item {
                SettingsSectionGroup(
                    title = "PROTECTION",
                    cardBg = cardBg,
                    isDark = isDark,
                    footerText = "Photos are taken silently with no shutter sound or flash."
                ) {
                    SettingsSwitchRow(
                        icon = Icons.Default.CameraAlt,
                        iconBg = Color(0xFF007AFF),
                        title = "Capture intruder photo",
                        checked = capturePhotoToggle,
                        onCheckedChange = { capturePhotoToggle = it }
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsValueRow(
                        icon = Icons.Default.CameraAlt,
                        iconBg = Color(0xFF8E8E93),
                        title = "Camera",
                        value = "Front >"
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsValueRow(
                        icon = Icons.Default.Collections,
                        iconBg = Color(0xFF5856D6),
                        title = "Photos per attempt",
                        value = "2 >"
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsValueRow(
                        icon = Icons.Default.Tag,
                        iconBg = Color(0xFFFF9500),
                        title = "Wrong attempts before capture",
                        value = "1 >"
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsSwitchRow(
                        icon = Icons.Default.LockOpen,
                        iconBg = EmeraldPrimary,
                        title = "Capture on successful unlock",
                        checked = captureOnUnlockToggle,
                        onCheckedChange = { captureOnUnlockToggle = it }
                    )
                }
            }

            // 4. ALERTS Grouped Bento Card
            item {
                SettingsSectionGroup(
                    title = "ALERTS",
                    cardBg = cardBg,
                    isDark = isDark
                ) {
                    SettingsSwitchRow(
                        icon = Icons.Default.Notifications,
                        iconBg = Color(0xFFFF3B30),
                        title = "Notifications",
                        checked = notificationsToggle,
                        onCheckedChange = { notificationsToggle = it }
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsSwitchRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        iconBg = Color(0xFFFF2D55),
                        title = "Alarm sound",
                        checked = alarmSoundToggle,
                        onCheckedChange = {
                            alarmSoundToggle = it
                            onAlarmToggle(it)
                        }
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsValueRow(
                        icon = Icons.Default.Email,
                        iconBg = Color(0xFF007AFF),
                        title = "Email alert",
                        value = "${uiState.alertEmailInterval} >"
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsSwitchRow(
                        icon = Icons.Default.NearMe,
                        iconBg = Color(0xFF5AC8FA),
                        title = "Include location",
                        checked = includeLocationToggle,
                        onCheckedChange = { includeLocationToggle = it }
                    )
                }
            }

            // 5. BACKUP & STORAGE Grouped Bento Card
            item {
                SettingsSectionGroup(
                    title = "BACKUP & STORAGE",
                    cardBg = cardBg,
                    isDark = isDark
                ) {
                    SettingsValueRow(
                        icon = Icons.Default.CloudDone,
                        iconBg = EmeraldPrimary,
                        title = "Google Drive",
                        value = "Connected >"
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsValueRow(
                        icon = Icons.Default.History,
                        iconBg = Color(0xFF8E8E93),
                        title = "Auto-delete after",
                        value = "${uiState.autoDeletePeriod} days >"
                    )
                    SettingsDivider(isDark = isDark)

                    // Storage Used Row with Progress Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Color(0xFF64748B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "Storage used",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    ),
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                            }

                            Text(
                                text = "46 MB of 500 MB",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = Color(0xFF64748B)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { 46f / 500f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = EmeraldPrimary,
                            trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // 6. PRIVACY & SECURITY Grouped Bento Card
            item {
                SettingsSectionGroup(
                    title = "PRIVACY & SECURITY",
                    cardBg = cardBg,
                    isDark = isDark,
                    footerText = "When hidden, open WTMP by dialing *#9867# from your phone app."
                ) {
                    SettingsSwitchRowWithValue(
                        icon = Icons.Default.Lock,
                        iconBg = EmeraldPrimary,
                        title = "App lock",
                        value = "Face ID",
                        checked = appLockToggle,
                        onCheckedChange = {
                            appLockToggle = it
                            onBiometricToggle(it)
                        }
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsSwitchRow(
                        icon = Icons.Default.VisibilityOff,
                        iconBg = Color(0xFF1E293B),
                        title = "Hide app icon",
                        checked = hideAppIconToggle,
                        onCheckedChange = { hideAppIconToggle = it }
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsSwitchRow(
                        icon = Icons.Default.Warning,
                        iconBg = Color(0xFFFF9500),
                        title = "Fake crash screen",
                        checked = fakeCrashToggle,
                        onCheckedChange = { fakeCrashToggle = it }
                    )
                }
            }

            // 7. SUPPORT Grouped Bento Card
            item {
                SettingsSectionGroup(
                    title = "SUPPORT",
                    cardBg = cardBg,
                    isDark = isDark
                ) {
                    SettingsValueRowWithIcon(
                        title = "Help centre",
                        trailingIcon = Icons.Default.OpenInNew
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsValueRow(
                        title = "Rate WTMP",
                        value = ">"
                    )
                    SettingsDivider(isDark = isDark)
                    SettingsValueRow(
                        title = "Privacy policy",
                        value = ">"
                    )
                }
            }

            // 8. Footer App Version Info
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "WTMP 3.2.1 (418)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =================================================================
// 3. MODULAR SUB-COMPOSABLES
// =================================================================

@Composable
fun SettingsSectionGroup(
    title: String,
    cardBg: Color,
    isDark: Boolean,
    footerText: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                fontSize = 12.sp
            ),
            color = Color(0xFF64748B),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        BentoCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                content()
            }
        }

        if (!footerText.isNullOrBlank()) {
            Text(
                text = footerText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchRow(
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
            .clickable { onCheckedChange(!checked) }
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
fun SettingsSwitchRowWithValue(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    value: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
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
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = Color(0xFF64748B),
            modifier = Modifier.padding(end = 8.dp)
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
fun SettingsValueRow(
    icon: ImageVector? = null,
    iconBg: Color? = null,
    title: String,
    value: String,
    onClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null && iconBg != null) {
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
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            color = if (isDark) Color.White else Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = Color(0xFF64748B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SettingsValueRowWithIcon(
    title: String,
    trailingIcon: ImageVector,
    onClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            imageVector = trailingIcon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SettingsDivider(isDark: Boolean) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp, end = 16.dp),
        thickness = 1.dp,
        color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    WTMPTheme {
        SettingsContent()
    }
}
