package com.milkywayy.ffmpegmediaencoder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.milkywayy.ffmpegmediaencoder.data.AppDatabase
import com.milkywayy.ffmpegmediaencoder.data.HistoryEntity
import com.milkywayy.ffmpegmediaencoder.data.LocalStrings
import com.milkywayy.ffmpegmediaencoder.ui.components.EmptyState
import com.milkywayy.ffmpegmediaencoder.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).historyDao() }
    val historyList by dao.getAllHistory().collectAsState(initial = emptyList())

    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Selection state
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val allSelected = historyList.isNotEmpty() && selectedIds.size == historyList.size

    // Confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(strings.deleteHistoryTitle) },
            text = {
                Text(
                    if (selectedIds.size == historyList.size)
                        strings.deleteAllHistoryConfirm.format(historyList.size)
                    else
                        strings.deleteSelectedHistoryConfirm.format(selectedIds.size)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        if (selectedIds.size == historyList.size) {
                            dao.deleteAll()
                        } else {
                            dao.deleteByIds(selectedIds.toList())
                        }
                        selectedIds = emptySet()
                    }
                    showDeleteDialog = false
                }) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            },
            shape = MaterialTheme.shapes.medium
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = strings.historyTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (historyList.isNotEmpty()) {
                Text(
                    text = strings.itemsCount.format(historyList.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action bar (visible when items exist)
        AnimatedVisibility(visible = historyList.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { checked ->
                            selectedIds = if (checked) historyList.map { it.id }.toSet()
                                          else emptySet()
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (allSelected) strings.deselectAll else strings.selectAll,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedIds.isNotEmpty()) {
                        Text(
                            text = strings.selectedCount.format(selectedIds.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                AnimatedVisibility(visible = selectedIds.isNotEmpty()) {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.delete)
                    }
                }
            }
        }

        if (historyList.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.History,
                title = strings.emptyHistoryTitle,
                description = strings.emptyHistoryDesc,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(historyList) { item ->
                    HistoryCard(
                        item = item,
                        sdf = sdf,
                        isSelected = selectedIds.contains(item.id),
                        onToggleSelect = { id ->
                            selectedIds = if (selectedIds.contains(id))
                                selectedIds - id
                            else
                                selectedIds + id
                        },
                        onDelete = {
                            coroutineScope.launch { dao.deleteById(item.id) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryEntity,
    sdf: SimpleDateFormat,
    isSelected: Boolean,
    onToggleSelect: (String) -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalStrings.current
    var expanded by remember { mutableStateOf(false) }
    val isSuccess = item.status == "SUCCESS"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox for selection
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect(item.id) },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Status icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSuccess) SuccessGreen.copy(alpha = 0.15f)
                            else ErrorRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Outlined.CheckCircle
                                      else Icons.Outlined.Error,
                        contentDescription = null,
                        tint = if (isSuccess) SuccessGreen else ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.inputFileName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                    Text(
                        text = "→ ${item.outputFileName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Row {
                    if (item.commandLine.isNotEmpty()) {
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (expanded) Icons.Outlined.ExpandLess
                                else Icons.Outlined.ExpandMore,
                                contentDescription = strings.detail,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = strings.delete,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Meta info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isSuccess) SuccessGreen.copy(alpha = 0.15f)
                            else ErrorRed.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isSuccess) strings.historySuccess else strings.historyFailed,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSuccess) SuccessGreen else ErrorRed
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sdf.format(Date(item.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.outputFormat.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.outputFormat.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expandable command section
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 28.dp, top = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalBackground)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = item.commandLine,
                            color = TerminalGreen,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (item.ffmpegVersion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FFmpeg: ${item.ffmpegVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
