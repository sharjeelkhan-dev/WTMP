package com.sharjeel.wtmp.ui.screens.stats

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sharjeel.wtmp.R
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.ui.components.BentoCard
import com.sharjeel.wtmp.ui.screens.dashboard.DockedBottomNavBar
import com.sharjeel.wtmp.ui.theme.WTMPTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val EmeraldPrimary = Color(0xFF059669)
private val AlertRed = Color(0xFFFF3B30)

// =================================================================
// 1. STATS SCREEN MAIN COMPOSABLE
// =================================================================

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDetails: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsScreenContent(
        uiState = uiState,
        onTabSelected = viewModel::setSelectedTab,
        onNavigateBack = onNavigateBack,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToDetails = onNavigateToDetails
    )
}

// =================================================================
// 2. STATELESS CONTENT COMPOSABLE
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreenContent(
    uiState: StatsUiState,
    onTabSelected: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var selectedFaceId by remember { mutableStateOf<String?>(null) }
    var showFacesDialog by remember { mutableStateOf(false) }
    var editingFace by remember { mutableStateOf<FaceItem?>(null) }
    var customFaceOverrides by remember { mutableStateOf(mapOf<String, Pair<String, Boolean>>()) }

    val allPhotoEvents = remember(uiState.events) {
        uiState.events.filter { !it.evidencePath.isNullOrBlank() }
    }

    val faces by remember(allPhotoEvents, customFaceOverrides) {
        mutableStateOf(
            buildGroupedFaces(
                photoEvents = allPhotoEvents,
                customOverrides = customFaceOverrides
            )
        )
    }

    val activeFace = remember(faces, selectedFaceId) {
        faces.firstOrNull { it.id == selectedFaceId }
    }

    val visiblePhotoEvents = remember(allPhotoEvents, activeFace) {
        if (activeFace != null && activeFace.associatedEvents.isNotEmpty()) {
            activeFace.associatedEvents
        } else {
            allPhotoEvents
        }
    }

    val filteredPhotoEvents = remember(visiblePhotoEvents, uiState.selectedTab) {
        when (uiState.selectedTab) {
            1 -> visiblePhotoEvents.filter { isIntruderEvent(it) }
            2 -> visiblePhotoEvents.filter { !isIntruderEvent(it) }
            else -> visiblePhotoEvents
        }
    }

    val groupedPhotos = remember(filteredPhotoEvents) {
        groupEventsByDateLabel(filteredPhotoEvents)
    }

    val headerPhotoCount = if (uiState.selectedTab == 0 && activeFace == null) {
        if (uiState.totalPhotoCount > 0) uiState.totalPhotoCount else allPhotoEvents.size
    } else {
        filteredPhotoEvents.size
    }

    val headerEventCount = if (uiState.selectedTab == 0 && activeFace == null) {
        if (uiState.totalEventCount > 0) uiState.totalEventCount else uiState.events.size
    } else {
        filteredPhotoEvents.size
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp),
                title = {
                    if (isSelectionMode && selectedIds.isNotEmpty()) {
                        Text(
                            text = "${selectedIds.size} selected",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                isSelectionMode = !isSelectionMode
                                if (!isSelectionMode) selectedIds = emptySet()
                            }
                        ) {
                            Text(
                                text = if (isSelectionMode) "Cancel" else "Select",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = EmeraldPrimary
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
                currentRoute = "stats",
                activityBadgeCount = uiState.unreadActivityCount,
                onRouteSelected = { route ->
                    when (route) {
                        "dashboard" -> onNavigateBack()
                        "history" -> onNavigateToHistory()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 0.dp,
                    bottom = 100.dp + navBarPadding
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Photos Header Title & Subtitle
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Photos",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 34.sp,
                                letterSpacing = (-0.6).sp
                            ),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$headerPhotoCount photos • $headerEventCount events",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // 2. Google Drive Sync Card
                item {
                    DashboardCardSurface(cardBg = cardBg) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Backed up to Google Drive",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Synced 2 min ago • 46 MB used",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // 3. FACES Section
                if (faces.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FACES",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = if (activeFace != null) "Clear" else "See all",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = EmeraldPrimary,
                                    modifier = Modifier.clickable {
                                        if (activeFace != null) {
                                            selectedFaceId = null
                                        } else {
                                            showFacesDialog = true
                                        }
                                    }
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(faces, key = { it.id }) { face ->
                                    FaceAvatarItem(
                                        face = face,
                                        isSelected = selectedFaceId == face.id,
                                        isDark = isDark,
                                        onClick = {
                                            selectedFaceId = if (selectedFaceId == face.id) null else face.id
                                        },
                                        onLongClick = {
                                            editingFace = face
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Segmented Control Filter Tabs ("All", "Intruders", "Unlocks")
                item {
                    PhotosSegmentedTabs(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = { index ->
                            selectedFaceId = null
                            onTabSelected(index)
                        },
                        isDark = isDark
                    )
                }

                // 5. 3-Column Date-Grouped Photo Grids
                if (filteredPhotoEvents.isEmpty()) {
                    item {
                        PhotosEmptyState(selectedTab = uiState.selectedTab)
                    }
                } else {
                    groupedPhotos.forEach { (dateHeader, eventsInGroup) ->
                        item(key = dateHeader) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateHeader,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.6.sp,
                                            fontSize = 12.sp
                                        ),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "${eventsInGroup.size} photos",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.sp
                                        ),
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                val rows = eventsInGroup.chunked(3)
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    rows.forEachIndexed { rowIndex, rowEvents ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            for (colIndex in 0 until 3) {
                                                val event = rowEvents.getOrNull(colIndex)
                                                if (event != null) {
                                                    val tileShape = getGridTileShape(
                                                        rowIndex = rowIndex,
                                                        colIndex = colIndex,
                                                        totalRows = rows.size,
                                                        itemsInCurrentRow = rowEvents.size
                                                    )
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        PhotoGridTile(
                                                            event = event,
                                                            shape = tileShape,
                                                            isSelectionMode = isSelectionMode,
                                                            isSelected = selectedIds.contains(event.id),
                                                            onClick = {
                                                                if (isSelectionMode) {
                                                                    selectedIds = if (selectedIds.contains(event.id)) {
                                                                        selectedIds - event.id
                                                                    } else {
                                                                        selectedIds + event.id
                                                                    }
                                                                } else {
                                                                    onNavigateToDetails(event.id)
                                                                }
                                                            }
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Footer Disclaimer Text
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Photos older than 30 days are deleted automatically. Change this in Settings > Backup & Storage.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }

    if (showFacesDialog) {
        FacesDirectoryDialog(
            faces = faces,
            onEditFace = { face -> editingFace = face },
            onDismiss = { showFacesDialog = false }
        )
    }

    if (editingFace != null) {
        RenamePersonDialog(
            face = editingFace!!,
            onSave = { newName, isKnownPerson ->
                val targetId = editingFace!!.id
                customFaceOverrides = customFaceOverrides + (targetId to Pair(newName, isKnownPerson))
                editingFace = null
            },
            onDismiss = { editingFace = null }
        )
    }
}

// =================================================================
// 3. FACE GROUPING LOGIC (ONE AVATAR PER PERSON, OWNER MERGED)
// =================================================================

private fun isIntruderEvent(event: SecurityEvent): Boolean {
    return event.type == SecurityEventType.FAILED_UNLOCK ||
            event.type == SecurityEventType.FAILED_ATTEMPT ||
            event.type == SecurityEventType.UNEXPECTED_UNLOCK
}

private fun isKnownOwnerEvent(event: SecurityEvent): Boolean {
    val stateText = event.deviceState.lowercase(Locale.getDefault())
    return stateText.contains("you") ||
            stateText.contains("owner") ||
            stateText.contains("self") ||
            stateText.contains("sharjeel") ||
            event.type == SecurityEventType.DEVICE_UNLOCKED
}

private fun resolvePersonClusterKey(event: SecurityEvent): String {
    val stateParts = event.deviceState
        .split("•")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val explicitPersonTag = stateParts.firstOrNull { part ->
        part.startsWith("Person:", ignoreCase = true) ||
                part.startsWith("Face:", ignoreCase = true) ||
                part.startsWith("Intruder:", ignoreCase = true)
    }

    if (explicitPersonTag != null) {
        val rawLabel = explicitPersonTag.substringAfter(":").trim()
        val lower = rawLabel.lowercase(Locale.getDefault())
        if (lower == "you" || lower == "owner" || lower == "self" || lower.contains("sharjeel")) {
            return "person_you"
        }
        if (lower == "intruder" || lower == "unknown" || lower == "stranger") {
            return "cluster_unknown"
        }
        if (rawLabel.isNotBlank()) {
            return "person_$lower"
        }
    }

    // Default: Group all photos captured on this device under the primary owner cluster ("person_you")
    return "person_you"
}

private fun buildGroupedFaces(
    photoEvents: List<SecurityEvent>,
    customOverrides: Map<String, Pair<String, Boolean>>
): List<FaceItem> {
    if (photoEvents.isEmpty()) return emptyList()

    val initialGroups = photoEvents
        .groupBy { resolvePersonClusterKey(it) }
        .map { (clusterId, personEvents) ->
            val sortedEvents = personEvents.sortedByDescending { it.timestamp }
            val defaultIsOwner = clusterId == "person_you"
            val defaultName = if (defaultIsOwner) "You" else "Unknown"

            val override = customOverrides[clusterId]
            val finalName = override?.first?.takeIf { it.isNotBlank() } ?: defaultName
            val isKnown = override?.second ?: defaultIsOwner

            val normalizedGroupId = if (
                finalName.equals("You", ignoreCase = true) ||
                finalName.equals("Owner", ignoreCase = true) ||
                finalName.equals("Self", ignoreCase = true) ||
                finalName.contains("sharjeel", ignoreCase = true)
            ) {
                "person_you"
            } else {
                clusterId
            }

            Triple(normalizedGroupId, Pair(finalName, isKnown), sortedEvents)
        }

    return initialGroups
        .groupBy { it.first }
        .map { (groupId, entries) ->
            val mergedEvents = entries
                .flatMap { it.third }
                .distinctBy { it.id }
                .sortedByDescending { it.timestamp }

            val (displayName, isKnown) = entries.first().second
            val isIntruder = !isKnown

            val representativePhoto = mergedEvents
                .firstOrNull { event ->
                    val path = event.evidencePath
                    !path.isNullOrBlank() && File(path).exists()
                }
                ?.evidencePath
                ?: mergedEvents.firstOrNull()?.evidencePath

            val countLabel = "${mergedEvents.size} ${if (mergedEvents.size == 1) "photo" else "photos"}"

            FaceItem(
                id = groupId,
                name = displayName,
                subtitle = countLabel,
                subtitleColor = if (isIntruder) AlertRed else Color(0xFF64748B),
                isIntruder = isIntruder,
                photoPath = representativePhoto,
                associatedEvents = mergedEvents
            )
        }
        .sortedByDescending { it.associatedEvents.size }
}

// =================================================================
// 4. MODULAR SUB-COMPOSABLES
// =================================================================

data class FaceItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val subtitleColor: Color,
    val isIntruder: Boolean,
    val photoPath: String? = null,
    val associatedEvents: List<SecurityEvent> = emptyList()
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FaceAvatarItem(
    face: FaceItem,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val ringColor = when {
        isSelected -> EmeraldPrimary
        face.isIntruder -> AlertRed
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .then(
                        if (ringColor != Color.Transparent) {
                            Modifier.border(2.dp, ringColor, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .padding(if (ringColor != Color.Transparent) 3.dp else 0.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (!face.photoPath.isNullOrBlank()) {
                    AsyncImage(
                        model = File(face.photoPath),
                        contentDescription = face.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = face.name,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            if (!face.isIntruder) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                        .border(
                            1.5.dp,
                            if (isDark) Color(0xFF0F172A) else Color(0xFFF3F4F6),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Text(
            text = face.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = if (isDark) Color.White else Color(0xFF0F172A)
        )

        Text(
            text = face.subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (face.isIntruder) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.sp
            ),
            color = face.subtitleColor
        )
    }
}

@Composable
fun RenamePersonDialog(
    face: FaceItem,
    onSave: (newName: String, isKnownPerson: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf(face.name) }
    var isKnownPerson by remember { mutableStateOf(!face.isIntruder) }
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, if (isKnownPerson) EmeraldPrimary else AlertRed, CircleShape)
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!face.photoPath.isNullOrBlank()) {
                        AsyncImage(
                            model = File(face.photoPath),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Edit Person Identity",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Assign a name to identify this person across all captured events and intruder logs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Person Name") },
                    placeholder = { Text("e.g. You, Sharjeel, Priya") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isKnownPerson = !isKnownPerson }
                        .background(if (isKnownPerson) EmeraldPrimary.copy(alpha = 0.12f) else Color(0xFF0F172A).copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mark as Known Person",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor
                    )
                    Checkbox(
                        checked = isKnownPerson,
                        onCheckedChange = { isKnownPerson = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldPrimary,
                            uncheckedColor = Color(0xFF64748B)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSave(nameInput.ifBlank { "Unknown" }, isKnownPerson)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save Person", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun FacesDirectoryDialog(
    faces: List<FaceItem>,
    onEditFace: (FaceItem) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Detected Faces Directory", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                TextButton(onClick = onDismiss) {
                    Text("Close", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Manage known persons and intruder groups detected by WTMP vision.", style = MaterialTheme.typography.bodySmall, color = mutedColor)
                Spacer(modifier = Modifier.height(4.dp))
                faces.forEach { face ->
                    Surface(
                        onClick = {
                            onDismiss()
                            onEditFace(face)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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
                                        .clip(CircleShape)
                                        .border(1.5.dp, if (face.isIntruder) AlertRed else EmeraldPrimary, CircleShape)
                                        .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!face.photoPath.isNullOrBlank()) {
                                        AsyncImage(
                                            model = File(face.photoPath),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(face.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                                    Text(face.subtitle, style = MaterialTheme.typography.bodySmall, color = face.subtitleColor)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (!face.isIntruder) {
                                    Surface(
                                        color = EmeraldPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Verified", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EmeraldPrimary)
                                    }
                                } else {
                                    Surface(
                                        color = AlertRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Intruder", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AlertRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun DashboardCardSurface(
    cardBg: Color,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    } else {
        Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(20.dp))
    }

    BentoCard(
        modifier = cardModifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        content()
    }
}

@Composable
fun PhotosSegmentedTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDark: Boolean
) {
    val containerBg = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0).copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("All", "Intruders", "Unlocks")
            tabs.forEachIndexed { index, label ->
                val isSelected = selectedTab == index
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
                    onClick = { onTabSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(11.dp),
                    color = pillBg,
                    shadowElevation = if (isSelected) (if (isDark) 3.dp else 1.5.dp) else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
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
fun PhotoGridTile(
    event: SecurityEvent,
    shape: Shape,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isIntruderEvent = isIntruderEvent(event)

    val timeLabel = remember(event.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .background(Color(0xFF334155))
            .clickable(onClick = onClick)
    ) {
        if (!event.evidencePath.isNullOrEmpty()) {
            AsyncImage(
                model = File(event.evidencePath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.no_image_photography_icon),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSelected) EmeraldPrimary.copy(alpha = 0.4f) else Color.Black.copy(
                            alpha = 0.2f
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) EmeraldPrimary else Color.Black.copy(alpha = 0.5f))
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isIntruderEvent) AlertRed else EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isIntruderEvent) R.drawable.person_svgrepo_com else R.drawable.unlock_icon
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

private fun getGridTileShape(
    rowIndex: Int,
    colIndex: Int,
    totalRows: Int,
    itemsInCurrentRow: Int
): Shape {
    val radius = 14.dp
    val isFirstRow = rowIndex == 0
    val isLastRow = rowIndex == totalRows - 1
    val isFirstCol = colIndex == 0
    val isLastColInRow = colIndex == itemsInCurrentRow - 1 && itemsInCurrentRow == 3

    val topStart = if (isFirstRow && isFirstCol) radius else 0.dp
    val topEnd = if (isFirstRow && isLastColInRow) radius else 0.dp
    val bottomStart = if (isLastRow && isFirstCol) radius else 0.dp
    val bottomEnd = if (isLastRow && (colIndex == 2 || (colIndex == itemsInCurrentRow - 1))) radius else 0.dp

    return RoundedCornerShape(
        topStart = topStart,
        topEnd = topEnd,
        bottomStart = bottomStart,
        bottomEnd = bottomEnd
    )
}

@Composable
fun PhotosEmptyState(selectedTab: Int) {
    val emptySubtitle = when (selectedTab) {
        1 -> "No intruder photos found for failed unlock attempts."
        2 -> "No unlock photos recorded yet."
        else -> "Intruder photos captured during failed unlocks will appear here automatically."
    }

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
                painter = painterResource(id = R.drawable.no_image_photography_icon),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = EmeraldPrimary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No photos captured yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = emptySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
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

@SuppressLint("AutoboxingStateCreation")
@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StatsScreenPreview() {
    val now = System.currentTimeMillis()

    WTMPTheme {
        StatsScreenContent(
            uiState = StatsUiState(
                events = listOf(
                    SecurityEvent(
                        type = SecurityEventType.FAILED_UNLOCK,
                        timestamp = now,
                        deviceState = "Wrong PIN • Home"
                    ),
                    SecurityEvent(
                        type = SecurityEventType.DEVICE_UNLOCKED,
                        timestamp = now - 3600000,
                        deviceState = "Fingerprint • Office"
                    ),
                    SecurityEvent(
                        type = SecurityEventType.DEVICE_UNLOCKED,
                        timestamp = now - 7200000,
                        deviceState = "Face Unlock • Home"
                    )
                ),
                isLoading = false
            ),
            onTabSelected = {},
            onNavigateBack = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToDetails = {}
        )
    }
}