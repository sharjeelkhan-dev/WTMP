package com.sharjeel.wtmp.ui.screens.settings

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.sharjeel.wtmp.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    SettingsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onThemeChange = viewModel::updateTheme,
        onBiometricToggle = viewModel::updateBiometric,
        onSensitivityChange = viewModel::updateSensitivity,
        onAutoDeleteChange = viewModel::updateAutoDeletePeriod,
        onAlarmToggle = viewModel::updateAlarm,
        onVibrationToggle = viewModel::updateVibration,
        onAntiTheftToggle = { enabled ->
            if (enabled) {
                // Guide to Accessibility Settings
                Toast.makeText(context, "Please enable WTMP Anti-Theft in Accessibility Settings", Toast.LENGTH_LONG).show()
                val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
            viewModel.updateAntiTheft(enabled)
        },
        onClearData = viewModel::clearData,
        onUninstallApp = {
            val packageName = context.packageName
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

            // Device Admin Check
            val isAdminActive = dpm?.activeAdmins?.any { it.packageName == packageName } == true

            if (isAdminActive) {
                Toast.makeText(
                    context,
                    "Please disable Device Admin permission first to uninstall.",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:$packageName".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                    data = "package:$packageName".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(uninstallIntent)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState = SettingsUiState(),
    onNavigateBack: () -> Unit = {},
    onThemeChange: (String) -> Unit = {},
    onBiometricToggle: (Boolean) -> Unit = {},
    onSensitivityChange: (Float) -> Unit = {},
    onAutoDeleteChange: (Int) -> Unit = {},
    onAlarmToggle: (Boolean) -> Unit = {},
    onVibrationToggle: (Boolean) -> Unit = {},
    onAntiTheftToggle: (Boolean) -> Unit = {},
    onClearData: () -> Unit = {},
    onUninstallApp: () -> Unit = {}
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = navBarPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection("General") {
                    SettingsItem(
                        painter = painterResource(id = R.drawable.bell_icon),
                        title = "Notifications",
                        showArrow = true,
                        onClick = { showNotificationsDialog = true }
                    )
                    SettingsDivider()
                    SettingsItem(
                        painter = painterResource(id = R.drawable.paint_palette_icon),
                        title = "Theme",
                        showArrow = true,
                        onClick = { showThemeDialog = true }
                    )
                }
            }

            item {
                SettingsSection("Security") {
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Protection & Privacy",
                        showArrow = true,
                        onClick = { showPrivacyDialog = true }
                    )
                    SettingsDivider()
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = onBiometricToggle
                    )
                }
            }

            item {
                SettingsSection("Anti-Theft") {
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Lock,
                        title = "Anti-Power Off",
                        checked = uiState.isAntiTheftEnabled,
                        onCheckedChange = onAntiTheftToggle
                    )
                    SettingsDivider()
                    Text(
                        text = "Intercepts clicks on 'Power off' or 'Restart' and forces biometric authentication, just like on high-end secure devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                SettingsSection("App") {
                    val context = LocalContext.current
                    SettingsItem(
                        icon = Icons.Default.Cloud,
                        title = "Cloud Sync",
                        showArrow = true,
                        onClick = {
                            Toast.makeText(context, "Cloud Sync coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onUninstallApp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    SettingsItem(
                        painter = painterResource(id = R.drawable.recycle_bin_icon),
                        title = "Uninstall App",
                        isDanger = true,
                        onClick = onUninstallApp
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        val options = listOf("System", "Light", "Dark")
        SelectionDialog(
            title = "Choose Theme",
            options = options,
            selectedOption = uiState.themeMode,
            onOptionSelected = {
                onThemeChange(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            isAlarmEnabled = uiState.isAlarmEnabled,
            isVibrationEnabled = uiState.isVibrationEnabled,
            onAlarmToggle = onAlarmToggle,
            onVibrationToggle = onVibrationToggle,
            onDismiss = { showNotificationsDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyDialog(
            sensitivity = uiState.detectionSensitivity,
            autoDeletePeriod = uiState.autoDeletePeriod,
            onSensitivityChange = onSensitivityChange,
            onAutoDeleteChange = onAutoDeleteChange,
            onClearHistory = {
                showPrivacyDialog = false
                showClearDataDialog = true
            },
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("This will permanently delete all security events and captured evidence. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NotificationsDialog(
    isAlarmEnabled: Boolean,
    isVibrationEnabled: Boolean,
    onAlarmToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications Settings") },
        text = {
            Column {
                SettingsItemWithSwitch(
                    icon = Icons.Default.Notifications,
                    title = "Alarm Sound",
                    checked = isAlarmEnabled,
                    onCheckedChange = onAlarmToggle
                )
                SettingsDivider()
                SettingsItemWithSwitch(
                    icon = Icons.Default.Notifications,
                    title = "Vibration",
                    checked = isVibrationEnabled,
                    onCheckedChange = onVibrationToggle
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun PrivacyDialog(
    sensitivity: Float,
    autoDeletePeriod: Int,
    onSensitivityChange: (Float) -> Unit,
    onAutoDeleteChange: (Int) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var showAutoDeleteDialog by remember { mutableStateOf(false) }

    if (showAutoDeleteDialog) {
        val options = mapOf(
            7 to "7 Days",
            30 to "30 Days",
            90 to "90 Days",
            0 to "Never"
        )
        val selectedLabel = when (autoDeletePeriod) {
            0 -> "Never"
            else -> "$autoDeletePeriod Days"
        }
        SelectionDialog(
            title = "Auto-delete Period",
            options = options.values.toList(),
            selectedOption = selectedLabel,
            onOptionSelected = { label ->
                val days = options.entries.find { it.value == label }?.key ?: 0
                onAutoDeleteChange(days)
                showAutoDeleteDialog = false
            },
            onDismiss = { showAutoDeleteDialog = false }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Protection & Privacy") },
            text = {
                Column {
                    SettingsItemWithSlider(
                        icon = Icons.Default.Speed,
                        title = "Detection Sensitivity",
                        value = sensitivity,
                        onValueChange = onSensitivityChange
                    )
                    SettingsDivider()
                    SettingsItemWithValue(
                        icon = Icons.Default.Timer,
                        title = "Auto-delete Period",
                        value = when (autoDeletePeriod) {
                            0 -> "Never"
                            else -> "$autoDeletePeriod Days"
                        },
                        onClick = { showAutoDeleteDialog = true }
                    )
                    SettingsDivider()
                    SettingsItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear All History",
                        isDanger = true,
                        onClick = onClearHistory
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun SettingsItemWithSwitch(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsItemContent(
        painter = rememberVectorPainter(icon),
        title = title,
        showArrow = false,
        isDanger = false,
        onClick = { onCheckedChange(!checked) }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsItemWithSlider(
    icon: ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = when {
                    value < 0.33f -> "Low"
                    value < 0.67f -> "Medium"
                    else -> "High"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.padding(top = 4.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
fun SettingsItemWithValue(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    SettingsItemContent(
        painter = rememberVectorPainter(icon),
        title = title,
        showArrow = true,
        isDanger = false,
        onClick = onClick
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = { onOptionSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
            fontWeight = FontWeight.Bold
        )
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    showArrow: Boolean = false,
    isDanger: Boolean = false,
    onClick: () -> Unit = {}
) {
    SettingsItemContent(
        painter = rememberVectorPainter(icon),
        title = title,
        showArrow = showArrow,
        isDanger = isDanger,
        onClick = onClick
    )
}

@Composable
fun SettingsItem(
    painter: Painter,
    title: String,
    showArrow: Boolean = false,
    isDanger: Boolean = false,
    onClick: () -> Unit = {}
) {
    SettingsItemContent(
        painter = painter,
        title = title,
        showArrow = showArrow,
        isDanger = isDanger,
        onClick = onClick
    )
}

@Composable
private fun SettingsItemContent(
    painter: Painter,
    title: String,
    showArrow: Boolean,
    isDanger: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val contentColor = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val iconBgColor = if (isDanger) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val iconTint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .then(modifier),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )

            if (trailingContent != null) {
                trailingContent()
            }

            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.8.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    com.sharjeel.wtmp.ui.theme.WTMPTheme {
        SettingsContent()
    }
}