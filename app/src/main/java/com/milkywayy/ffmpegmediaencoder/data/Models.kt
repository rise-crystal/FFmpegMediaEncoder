package com.milkywayy.ffmpegmediaencoder.data

import java.time.LocalDateTime

enum class QueueStatus {
    WAITING, PROCESSING, SUCCESS, FAILED, CANCELLED
}

data class MediaQueueItem(
    val id: String,
    val inputUri: String,
    val inputFileName: String,
    val inputFileSize: Long,
    val inputFormat: String,
    var durationMs: Long = 0,
    var outputFileName: String = "",
    var outputFormat: String = "",
    var outputUri: String? = null,
    var status: QueueStatus = QueueStatus.WAITING,
    var progress: Int = 0,
    var errorMessage: String? = null
)

data class ConversionJob(
    val id: String,
    val items: List<MediaQueueItem>,
    val outputFormat: String,
    val preset: String,
    val outputFolderUri: String,
    val deleteOriginalAfterSuccess: Boolean,
    var status: QueueStatus = QueueStatus.WAITING,
    var totalProgress: Int = 0
)

data class ConversionLog(
    val id: String,
    val message: String
)
