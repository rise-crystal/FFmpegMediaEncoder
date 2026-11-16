package com.milkywayy.ffmpegmediaencoder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val inputFileName: String,
    val outputFileName: String,
    val outputFormat: String,
    val status: String,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val ffmpegVersion: String = "",
    val commandLine: String = ""
)
