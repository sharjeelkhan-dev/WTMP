package com.sharjeel.wtmp.ui.screens.history

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.ui.screens.dashboard.DockedBottomNavBar
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val EmeraldPrimary = Color(0xFF047857)
private val SwitchActiveGreen = Color(0xFF22C55E)
private val AlertRed = Color(0xFFEF4444)

@Composable
private fun DashboardStyleCard(
    modifier: Modifier = Modifier,
    cardBg: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        content = content
    )
}

// =================================================================
// 1. MAIN SCREEN ENTRY POINT (Stateful Composable)
// =================================================================

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToDetails: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.markActivityAsVisited()
    }

    HistoryScreenContent(
        uiState = uiState,
        searchQuery = uiState.searchQuery,
        onSearchQueryChanged = viewModel::searchEvents,
        onFilterSelected = viewModel::setFilter,
        onNavigateToDetails = onNavigateToDetails,
        onBackClick = onBackClick,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToStats = onNavigateToStats
    )
}

// =================================================================
// 2. STATELESS CONTENT COMPOSABLE
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenContent(
    uiState: HistoryUiState,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (HistoryFilter) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val dividerColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val groupedEvents = remember(uiState.events) {
        groupEventsByDateLabel(uiState.events)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp),
                title = {},
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onBackClick)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Home",
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
                        TextButton(onClick = { /* Select */ }) {
                            Text(
                                text = "Select",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color =  MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBg
                )
            )
        },
        containerColor = screenBg,
        bottomBar = {
            DockedBottomNavBar(
                currentRoute = "history",
                activityBadgeCount = uiState.unreadActivityCount,
                onRouteSelected = { route ->
                    when (route) {
                        "dashboard" -> onBackClick()
                        "stats" -> onNavigateToStats()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = 100.dp + navBarPadding
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Screen Title: Activity
            item {
                Text(
                    text = "Activity",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }

            // 2. Search Box: "Search events, places"
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Search events, places",
                            style = MaterialTheme.typography.bodyMedium,
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
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
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

            // 3. Segmented Control Tabs ("All", "Failed", "Unlocks")
            item {
                SegmentedControlTabs(
                    currentFilter = uiState.currentFilter,
                    onFilterSelected = onFilterSelected,
                    isDark = isDark
                )
            }

            // 4. This Week Overview Card (Exact position as image_8a3aa5.png)
            item {
                ActivityWeekOverviewCard(
                    cardBg = cardBg,
                    failedCount = uiState.events.count { it.type == SecurityEventType.FAILED_UNLOCK },
                    unlocksCount = uiState.events.count {
                        it.type == SecurityEventType.DEVICE_UNLOCKED ||
                                it.type == SecurityEventType.UNEXPECTED_UNLOCK
                    },
                    photosCount = uiState.events.count {
                        !it.evidencePath.isNullOrEmpty() || it.type == SecurityEventType.FAILED_UNLOCK
                    }
                )
            }

            // 5. Grouped Events List by Date
            if (uiState.isLoading) {
                items(3) {
                    ShimmerActivityCard(cardBg = cardBg)
                }
            } else if (uiState.events.isEmpty()) {
                item {
                    HistoryEmptyState(cardBg = cardBg)
                }
            } else {
                groupedEvents.forEach { (dateHeader, eventsInGroup) ->
                    item(key = dateHeader) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            DashboardStyleCard(cardBg = cardBg) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    eventsInGroup.forEachIndexed { index, event ->
                                        ActivityEventRow(
                                            event = event,
                                            onClick = { onNavigateToDetails(event.id) }
                                        )
                                        if (index < eventsInGroup.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(start = 70.dp, end = 16.dp),
                                                color = dividerColor,
                                                thickness = 1.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Footer Disclaimer Text
                item {
                    Text(
                        text = "History is kept for 30 days. Change this in Settings > Storage.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

// =================================================================
// 3. MODULAR SUB-COMPOSABLES
// =================================================================

@Composable
fun SegmentedControlTabs(
    currentFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit,
    isDark: Boolean
) {
    val containerBg = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0).copy(alpha = 0.65f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                HistoryFilter.ALL to "All",
                HistoryFilter.WEEK to "Failed",
                HistoryFilter.MONTH to "Unlocks"
            )

            tabs.forEach { (filterOption, label) ->
                val isSelected = currentFilter == filterOption
                val pillBg = if (isSelected) {
                    if (isDark) Color(0xFF334155) else Color.White
                } else {
                    Color.Transparent
                }
                val textColor = if (isSelected) {
                    if (isDark) Color.White else Color(0xFF0F172A)
                } else {
                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                }

                Surface(
                    onClick = { onFilterSelected(filterOption) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = pillBg,
                    shadowElevation = if (isSelected) (if (isDark) 4.dp else 2.dp) else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityWeekOverviewCard(
    cardBg: Color,
    failedCount: Int,
    unlocksCount: Int,
    photosCount: Int
) {
    val isDark = isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else Color(0xFF0F172A)

    DashboardStyleCard(cardBg = cardBg) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "This week",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCol(
                    value = "$failedCount",
                    label = "Failed",
                    valueColor = AlertRed
                )
                StatCol(
                    value = "$unlocksCount",
                    label = "Unlocks",
                    valueColor = primaryText
                )
                StatCol(
                    value = "$photosCount",
                    label = "Photos",
                    valueColor = primaryText
                )
            }
        }
    }
}

@Composable
fun StatCol(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

sealed class EventRowIcon {
    data class Vector(val imageVector: ImageVector) : EventRowIcon()
    data class Drawable(val resId: Int) : EventRowIcon()
}

@Composable
fun ActivityEventRow(
    event: SecurityEvent,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isUnlockSuccess = event.type == SecurityEventType.DEVICE_UNLOCKED ||
            event.type == SecurityEventType.UNEXPECTED_UNLOCK
    val displayTitle = if (isUnlockSuccess) "Device unlocked" else "Failed unlock"
    val timeLabel = formatEventTimeLabel(event.timestamp)

    val (iconBg, iconTint, rowIcon) = when {
        !isUnlockSuccess && (
                event.deviceState.contains("pattern", ignoreCase = true) ||
                        event.deviceState.contains("camera", ignoreCase = true)
                ) -> {
            Triple(
                Color(0xFFF1F5F9),
                Color(0xFF64748B),
                EventRowIcon.Drawable(R.drawable.no_image_photography_icon)
            )
        }
        !isUnlockSuccess -> {
            Triple(
                Color(0xFFFEE2E2),
                Color(0xFFDC2626),
                EventRowIcon.Drawable(R.drawable.cross_icon)
            )
        }
        event.deviceState.contains("face", ignoreCase = true) -> {
            Triple(
                Color(0xFFF1F5F9),
                Color(0xFF64748B),
                EventRowIcon.Drawable(R.drawable.unlock_icon)
            )
        }
        else -> {
            Triple(
                Color(0xFFD1FAE5),
                EmeraldPrimary,
                EventRowIcon.Vector(Icons.Default.Fingerprint)
            )
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
            when (rowIcon) {
                is EventRowIcon.Vector -> {
                    Icon(
                        imageVector = rowIcon.imageVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                is EventRowIcon.Drawable -> {
                    Icon(
                        painter = painterResource(id = rowIcon.resId),
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
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
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
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

@Composable
fun ShimmerActivityCard(cardBg: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    DashboardStyleCard(cardBg = cardBg) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFCBD5E1).copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFCBD5E1).copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFCBD5E1).copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
fun HistoryEmptyState(cardBg: Color) {
    DashboardStyleCard(cardBg = cardBg) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = EmeraldPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No activity logs available",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "All recorded intrusion attempts and device unlock logs will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun groupEventsByDateLabel(events: List<SecurityEvent>): List<Pair<String, List<SecurityEvent>>> {
    val nowCal = Calendar.getInstance()
    val today = nowCal.get(Calendar.DAY_OF_YEAR)
    val year = nowCal.get(Calendar.YEAR)

    val eventCal = Calendar.getInstance()

    return events.groupBy { event ->
        eventCal.timeInMillis = event.timestamp
        val eventDay = eventCal.get(Calendar.DAY_OF_YEAR)
        val eventYear = eventCal.get(Calendar.YEAR)

        when {
            eventYear == year && eventDay == today -> "TODAY"
            eventYear == year && eventDay == today - 1 -> "YESTERDAY"
            else -> SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
                .format(Date(event.timestamp))
                .uppercase()
        }
    }.toList()
}

private fun formatEventTimeLabel(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HistoryScreenPreview() {
    val now = System.currentTimeMillis()

    WTMPTheme {
        HistoryScreenContent(
            uiState = HistoryUiState(
                events = listOf(
                    SecurityEvent(
                        type = SecurityEventType.FAILED_UNLOCK,
                        timestamp = now,
                        deviceState = "Wrong PIN x3 • 2 photos • Home"
                    ),
                    SecurityEvent(
                        type = SecurityEventType.DEVICE_UNLOCKED,
                        timestamp = now - 3600000,
                        deviceState = "Fingerprint • 2 photos • Office"
                    )
                ),
                isLoading = false
            ),
            searchQuery = "",
            onSearchQueryChanged = {},
            onFilterSelected = {},
            onNavigateToDetails = {},
            onBackClick = {}
        )
    }
}