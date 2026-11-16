package com.milkywayy.ffmpegmediaencoder

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.milkywayy.ffmpegmediaencoder.data.ConversionLog
import com.milkywayy.ffmpegmediaencoder.data.ConversionManager
import com.milkywayy.ffmpegmediaencoder.data.MediaQueueItem
import com.milkywayy.ffmpegmediaencoder.service.EncoderService
import kotlinx.coroutines.flow.StateFlow

class EncoderViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("EncoderPrefs", Context.MODE_PRIVATE)

    init {
        ConversionManager.init(application, prefs)
    }

    val queue: StateFlow<List<MediaQueueItem>> = ConversionManager.queue
    val outputFormat: StateFlow<String> = ConversionManager.outputFormat
    val preset: StateFlow<String> = ConversionManager.preset
    val outputFolderUri: StateFlow<Uri?> = ConversionManager.outputFolderUri
    val deleteOriginal: StateFlow<Boolean> = ConversionManager.deleteOriginal
    val isConverting: StateFlow<Boolean> = ConversionManager.isConverting
    val logs: StateFlow<List<ConversionLog>> = ConversionManager.logs
    val totalProgress: StateFlow<Int> = ConversionManager.totalProgress
    val selectedQueueItems: StateFlow<Set<String>> = ConversionManager.selectedQueueItems
    val totalEta: StateFlow<String> = ConversionManager.totalEta
    val currentFileIndex: StateFlow<Int> = ConversionManager.currentFileIndex
    val totalFiles: StateFlow<Int> = ConversionManager.totalFiles

    fun setOutputFormat(format: String) {
        ConversionManager.setOutputFormat(format, prefs)
    }

    fun setPreset(preset: String) {
        ConversionManager.setPreset(preset, prefs)
    }

    fun setOutputFolderUri(uri: Uri?) {
        ConversionManager.setOutputFolderUri(uri, prefs)
    }

    fun setDeleteOriginal(delete: Boolean) {
        ConversionManager.setDeleteOriginal(delete)
    }

    fun addMediaFiles(context: Context, uris: List<Uri>) {
        ConversionManager.addMediaFiles(context, uris)
    }

    fun removeMediaItem(id: String) {
        ConversionManager.removeMediaItem(id)
    }

    fun toggleSelection(id: String) {
        ConversionManager.toggleSelection(id)
    }

    fun selectAllQueueItems() {
        ConversionManager.selectAllQueueItems()
    }

    fun deleteSelectedQueueItems() {
        ConversionManager.deleteSelectedQueueItems()
    }

    fun startConversion(context: Context) {
        if (ConversionManager.isConverting.value) return
        if (ConversionManager.queue.value.isEmpty()) return
        if (ConversionManager.outputFolderUri.value == null) {
            ConversionManager.addLog("Error: Folder output belum dipilih.")
            return
        }

        EncoderService.start(context)
    }

    fun cancelConversion() {
        EncoderService.stop(getApplication())
        ConversionManager.cancelConversion()
    }
}
