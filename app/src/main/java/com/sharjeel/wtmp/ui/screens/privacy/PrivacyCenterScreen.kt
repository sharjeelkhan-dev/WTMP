package com.sharjeel.wtmp.ui.screens.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharjeel.wtmp.ui.components.GlassCard
import com.sharjeel.wtmp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy Center", color = WtmpPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WtmpPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = WtmpPurpleBackground
                )
            )
        },
        containerColor = WtmpPurpleBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
        ) {
            item {
                PrivacyCard(
                    title = "Local Data Storage",
                    description = "All security data remains local to this device. We do not use cloud backups for your history.",
                    icon = Icons.Default.Lock
                )
            }
            
            item {
                PrivacyCard(
                    title = "Secured Photos",
                    description = "Photos are encrypted and secured in private app storage, inaccessible to other apps or galleries.",
                    icon = Icons.Default.Lock
                )
            }
            
            item {
                PrivacyCard(
                    title = "Zero External Servers",
                    description = "No data, metadata, or logs are sent to external servers. Your privacy is enforced by architecture.",
                    icon = Icons.Default.Done
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                DeleteDataSection()
            }
        }
    }
}

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
                tint = WtmpPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = WtmpTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WtmpTextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun DeleteDataSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.labelLarge,
            color = WtmpTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Button(
            onClick = { /* TODO: Implement global delete */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = WtmpRed.copy(alpha = 0.1f),
                contentColor = WtmpRed
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, WtmpRed.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete All Application Data", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "This action is permanent and cannot be undone.",
            style = MaterialTheme.typography.bodySmall,
            color = WtmpRed.copy(alpha = 0.7f)
        )
    }
}
