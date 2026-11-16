package com.milkywayy.ffmpegmediaencoder.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.milkywayy.ffmpegmediaencoder.data.LocalStrings
import com.milkywayy.ffmpegmediaencoder.data.MediaQueueItem
import com.milkywayy.ffmpegmediaencoder.data.QueueStatus
import com.milkywayy.ffmpegmediaencoder.ui.theme.*

@Composable
fun QueueItemCard(
    item: MediaQueueItem,
    isSelected: Boolean,
    onToggleSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val statusColor by animateColorAsState(
        targetValue = when (item.status) {
            QueueStatus.SUCCESS -> SuccessGreen
            QueueStatus.FAILED -> ErrorRed
            QueueStatus.PROCESSING -> ProcessingBlue
            QueueStatus.CANCELLED -> CancelledGray
            QueueStatus.WAITING -> WarningYellow
        },
        label = "statusColor"
    )

    val progressAnimated by animateFloatAsState(
        targetValue = item.progress / 100f,
        label = "progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                // File type icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getFileIcon(item.inputFormat),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // File info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.inputFileName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status chip
                        StatusChip(status = item.status)
                        // File size
                        if (item.inputFileSize > 0) {
                            Text(
                                text = formatFileSize(item.inputFileSize),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Actions
                Row {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect(item.id) },
                        modifier = Modifier.size(24.dp)
                    )
                    IconButton(
                        onClick = { onRemove(item.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = strings.delete,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Progress bar (only when processing or done)
            if (item.status == QueueStatus.PROCESSING || item.status == QueueStatus.SUCCESS) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = progressAnimated,
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text(
                        text = "${item.progress}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: QueueStatus) {
    val strings = LocalStrings.current
    val (backgroundColor, textColor, label) = when (status) {
        QueueStatus.WAITING -> Triple(WarningYellow.copy(alpha = 0.15f), WarningYellow, strings.statusWaiting)
        QueueStatus.PROCESSING -> Triple(ProcessingBlue.copy(alpha = 0.15f), ProcessingBlue, strings.statusProcessing)
        QueueStatus.SUCCESS -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, strings.statusSuccess)
        QueueStatus.FAILED -> Triple(ErrorRed.copy(alpha = 0.15f), ErrorRed, strings.statusFailed)
        QueueStatus.CANCELLED -> Triple(CancelledGray.copy(alpha = 0.15f), CancelledGray, strings.statusCancelled)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

private fun getFileIcon(ext: String): ImageVector {
    return when (ext.lowercase()) {
        "mp4", "mkv", "mov", "avi", "webm", "3gp", "ts", "flv", "wmv" -> Icons.Outlined.Videocam
        "mp3", "aac", "wav", "flac", "ogg", "m4a" -> Icons.Outlined.AudioFile
        else -> Icons.Outlined.InsertDriveFile
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
