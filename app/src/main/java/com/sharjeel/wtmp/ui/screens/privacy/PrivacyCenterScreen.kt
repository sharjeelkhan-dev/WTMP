package com.sharjeel.wtmp.ui.screens.privacy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharjeel.wtmp.ui.components.GlassCard

// =================================================================
// 1. MAIN PRIVACY CENTER SCREEN (Stateless Composable)
// =================================================================

/**
 * PrivacyCenterScreen outlines security commitments (local storage, encrypted photos,
 * zero external server routing) and offers global data reset actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    onNavigateBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Privacy Center",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
        ) {
            // Local Storage Feature Card
            item {
                PrivacyCard(
                    title = "Local Data Storage",
                    description = "All security data remains local to this device. We do not use cloud backups for your history.",
                    icon = Icons.Default.Lock
                )
            }

            // Encrypted Photos Card
            item {
                PrivacyCard(
                    title = "Secured Photos",
                    description = "Photos are encrypted and secured in private app storage, inaccessible to other apps or galleries.",
                    icon = Icons.Default.Lock
                )
            }

            // Architecture Isolation Card
            item {
                PrivacyCard(
                    title = "Zero External Servers",
                    description = "No data, metadata, or logs are sent to external servers. Your privacy is enforced by architecture.",
                    icon = Icons.Default.Done
                )
            }

            // Destructive Data Clearing Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                DeleteDataSection()
            }
        }
    }
}

// =================================================================
// 2. SUB-COMPOSABLES
// =================================================================

/**
 * Feature card wrapper utilizing custom GlassCard UI component for privacy assurances.
 */
@Composable
fun PrivacyCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * Destructive action section allowing complete application data wipe.
 */
@Composable
fun DeleteDataSection() {
    val errorColor = MaterialTheme.colorScheme.error

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Button(
            onClick = { /* TODO: Implement global delete */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = errorColor.copy(alpha = 0.1f),
                contentColor = errorColor
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, errorColor.copy(alpha = 0.5f))
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Delete All Application Data", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This action is permanent and cannot be undone.",
            style = MaterialTheme.typography.bodySmall,
            color = errorColor.copy(alpha = 0.7f)
        )
    }
}