package com.milkywayy.ffmpegmediaencoder.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.milkywayy.ffmpegmediaencoder.EncoderViewModel
import com.milkywayy.ffmpegmediaencoder.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EncoderViewModel,
    onNavigateToLog: () -> Unit = {}
) {
    val context = LocalContext.current
    val queue by viewModel.queue.collectAsState()
    val outputFormat by viewModel.outputFormat.collectAsState()
    val preset by viewModel.preset.collectAsState()
    val outputFolderUri by viewModel.outputFolderUri.collectAsState()
    val deleteOriginal by viewModel.deleteOriginal.collectAsState()
    val isConverting by viewModel.isConverting.collectAsState()
    val selectedItems by viewModel.selectedQueueItems.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addMediaFiles(context, uris)
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.setOutputFolderUri(it)
        }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Konfirmasi") },
            text = { Text("Apakah Anda yakin ingin menghapus file asli setelah konversi berhasil? File tidak dapat dikembalikan.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDeleteOriginal(true)
                    showConfirmDialog = false
                }) {
                    Text("Ya, Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            },
            shape = MaterialTheme.shapes.medium
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { filePickerLauncher.launch(arrayOf("video/*", "audio/*")) },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Tambah File") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Queue section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Queue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Antrian (${queue.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                AnimatedVisibility(visible = queue.isNotEmpty()) {
                    Row {
                        IconButton(onClick = { viewModel.selectAllQueueItems() }) {
                            Icon(
                                Icons.Outlined.SelectAll,
                                contentDescription = "Pilih Semua",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { viewModel.deleteSelectedQueueItems() },
                            enabled = selectedItems.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Hapus Terpilih",
                                tint = if (selectedItems.isNotEmpty()) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            // Queue list or empty state
            if (queue.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.MovieFilter,
                    title = "Tidak ada antrian",
                    description = "Ketuk tombol + Tambah File untuk memulai proses konversi.",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(queue) { item ->
                        QueueItemCard(
                            item = item,
                            isSelected = selectedItems.contains(item.id),
                            onToggleSelect = { viewModel.toggleSelection(it) },
                            onRemove = { viewModel.removeMediaItem(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Pengaturan Konversi",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Dropdowns in a row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppDropdown(
                            label = "Format",
                            selectedValue = outputFormat,
                            options = listOf("mp4", "mkv", "mov", "avi", "webm", "mp3", "aac"),
                            onValueSelected = { viewModel.setOutputFormat(it) },
                            modifier = Modifier.weight(1f)
                        )
                        AppDropdown(
                            label = "Preset",
                            selectedValue = preset,
                            options = listOf("Original Quality", "Compress Video", "720p", "Video to MP3"),
                            onValueSelected = { viewModel.setPreset(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Output folder button
                    OutlinedButton(
                        onClick = { folderPickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            Icons.Outlined.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (outputFolderUri != null) "Output: ${outputFolderUri?.lastPathSegment}"
                                   else "Pilih Folder Output",
                            maxLines = 1
                        )
                    }

                    // Delete original checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = deleteOriginal,
                            onCheckedChange = { checked ->
                                if (checked) showConfirmDialog = true
                                else viewModel.setDeleteOriginal(false)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hapus file asli setelah konversi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Convert button
            Button(
                onClick = {
                    viewModel.startConversion(context)
                    onNavigateToLog()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isConverting && queue.isNotEmpty() && outputFolderUri != null,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Icon(Icons.Outlined.RocketLaunch, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isConverting) "Mengonversi..." else "Mulai Konversi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}
