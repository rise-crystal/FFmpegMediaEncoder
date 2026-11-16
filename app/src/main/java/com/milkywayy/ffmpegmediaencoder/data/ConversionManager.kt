package com.milkywayy.ffmpegmediaencoder.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object ConversionManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _queue = MutableStateFlow<List<MediaQueueItem>>(emptyList())
    val queue: StateFlow<List<MediaQueueItem>> = _queue.asStateFlow()

    private val _outputFormat = MutableStateFlow("mp4")
    val outputFormat: StateFlow<String> = _outputFormat.asStateFlow()

    private val _preset = MutableStateFlow("Original Quality")
    val preset: StateFlow<String> = _preset.asStateFlow()

    private val _outputFolderUri = MutableStateFlow<Uri?>(null)
    val outputFolderUri: StateFlow<Uri?> = _outputFolderUri.asStateFlow()

    private val _deleteOriginal = MutableStateFlow(false)
    val deleteOriginal: StateFlow<Boolean> = _deleteOriginal.asStateFlow()

    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

    private val _logs = MutableStateFlow<List<ConversionLog>>(emptyList())
    val logs: StateFlow<List<ConversionLog>> = _logs.asStateFlow()

    private val _totalProgress = MutableStateFlow(0)
    val totalProgress: StateFlow<Int> = _totalProgress.asStateFlow()

    private val _selectedQueueItems = MutableStateFlow<Set<String>>(emptySet())
    val selectedQueueItems: StateFlow<Set<String>> = _selectedQueueItems.asStateFlow()

    private val _totalEta = MutableStateFlow("")
    val totalEta: StateFlow<String> = _totalEta.asStateFlow()

    private val _currentFileIndex = MutableStateFlow(0)
    val currentFileIndex: StateFlow<Int> = _currentFileIndex.asStateFlow()

    private val _totalFiles = MutableStateFlow(0)
    val totalFiles: StateFlow<Int> = _totalFiles.asStateFlow()

    private var isInitialized = false

    fun init(context: Context, prefs: SharedPreferences) {
        if (isInitialized) return
        isInitialized = true

        cleanOrphanedCacheFiles(context)

        _outputFormat.value = prefs.getString("outputFormat", "mp4") ?: "mp4"
        _preset.value = prefs.getString("preset", "Original Quality") ?: "Original Quality"

        val savedFolder = prefs.getString("outputFolderUri", null)
        _outputFolderUri.value = savedFolder?.let { Uri.parse(it) } ?: getDefaultOutputFolder(context)
    }

    private fun getDefaultOutputFolder(context: Context): Uri {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "FFmpegEncoder")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return Uri.fromFile(dir)
    }

    fun cleanOrphanedCacheFiles(context: Context) {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("ffmpeg_in_") || file.name.startsWith("ffmpeg_out_")) {
                    file.delete()
                }
            }
        } catch (_: Exception) {}
    }

    fun isAudioFormat(format: String): Boolean {
        return format.lowercase().trim() in setOf("mp3", "aac", "wav", "flac", "ogg", "oga", "m4a", "opus")
    }

    fun getMimeType(format: String): String {
        return when (format.lowercase().trim()) {
            "mp3" -> "audio/mpeg"
            "aac" -> "audio/aac"
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            "ogg", "oga" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "opus" -> "audio/opus"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "webm" -> "video/webm"
            "3gp", "3g2" -> "video/3gpp"
            "ts", "mts", "m2ts" -> "video/mp2t"
            "flv" -> "video/x-flv"
            "wmv" -> "video/x-ms-wmv"
            else -> if (isAudioFormat(format)) "audio/*" else "video/*"
        }
    }

    fun setOutputFormat(format: String, prefs: SharedPreferences) {
        _outputFormat.value = format
        prefs.edit().putString("outputFormat", format).apply()

        // Sync preset if format changed to audio
        if (isAudioFormat(format) && _preset.value != "Video to MP3") {
            setPreset("Video to MP3", prefs)
        }
    }

    fun setPreset(preset: String, prefs: SharedPreferences) {
        _preset.value = preset
        prefs.edit().putString("preset", preset).apply()

        // Sync format if preset changed to audio extraction
        if (preset == "Video to MP3" && !isAudioFormat(_outputFormat.value)) {
            setOutputFormat("mp3", prefs)
        }
    }

    fun setOutputFolderUri(uri: Uri?, prefs: SharedPreferences) {
        _outputFolderUri.value = uri
        prefs.edit().putString("outputFolderUri", uri?.toString()).apply()
    }

    fun setDeleteOriginal(delete: Boolean) {
        _deleteOriginal.value = delete
    }

    fun addMediaFiles(context: Context, uris: List<Uri>) {
        scope.launch {
            val newItems = uris.mapNotNull { uri ->
                var name = "Unknown"
                var size = 0L
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        name = if (nameIndex != -1) cursor.getString(nameIndex) else "Unknown"
                        size = if (sizeIndex != -1) cursor.getLong(sizeIndex) else 0L
                    }
                }

                val ext = name.substringAfterLast('.', "")
                MediaQueueItem(
                    id = UUID.randomUUID().toString(),
                    inputUri = uri.toString(),
                    inputFileName = name,
                    inputFileSize = size,
                    inputFormat = ext
                )
            }
            _queue.value = _queue.value + newItems
        }
    }

    fun removeMediaItem(id: String) {
        _queue.value = _queue.value.filter { it.id != id }
        _selectedQueueItems.value = _selectedQueueItems.value - id
    }

    fun toggleSelection(id: String) {
        val current = _selectedQueueItems.value
        if (current.contains(id)) {
            _selectedQueueItems.value = current - id
        } else {
            _selectedQueueItems.value = current + id
        }
    }

    fun selectAllQueueItems() {
        val allIds = _queue.value.map { it.id }.toSet()
        if (_selectedQueueItems.value.size == allIds.size) {
            _selectedQueueItems.value = emptySet()
        } else {
            _selectedQueueItems.value = allIds
        }
    }

    fun deleteSelectedQueueItems() {
        val selected = _selectedQueueItems.value
        _queue.value = _queue.value.filter { !selected.contains(it.id) }
        _selectedQueueItems.value = emptySet()
    }

    fun addLog(message: String) {
        val newLog = ConversionLog(UUID.randomUUID().toString(), message)
        val currentList = _logs.value
        // Cap logs at 500 lines to prevent UI lag and memory pressure
        _logs.value = if (currentList.size >= 500) {
            currentList.drop(currentList.size - 499) + newLog
        } else {
            currentList + newLog
        }
    }

    fun cancelConversion() {
        _isConverting.value = false
        FFmpegKit.cancel()
        addLog("Semua proses konversi dibatalkan.")
        _queue.value = _queue.value.map {
            if (it.status == QueueStatus.PROCESSING || it.status == QueueStatus.WAITING) {
                it.copy(status = QueueStatus.CANCELLED)
            } else {
                it
            }
        }
    }

    private fun updateItemStatus(id: String, status: QueueStatus) {
        _queue.value = _queue.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    private fun updateItemProgress(id: String, progress: Int) {
        _queue.value = _queue.value.map { if (it.id == id) it.copy(progress = progress) else it }
    }

    private fun getInputFormatHint(ext: String): String {
        return when (ext.lowercase().trim()) {
            "ts", "mts", "m2ts", "m2t" -> "-f mpegts"
            "mkv"                       -> "-f matroska"
            "avi"                       -> "-f avi"
            "flv"                       -> "-f flv"
            "mov", "qt"                 -> "-f mov"
            "mp4", "m4v"               -> "-f mp4"
            "webm"                      -> "-f webm"
            "wmv"                       -> "-f asf"
            "3gp", "3g2"               -> "-f 3gp"
            else                        -> ""
        }
    }

    private fun getMp4OutputFlags(): String = "-movflags +faststart"

    private fun buildCommand(
        input: String,
        output: String,
        preset: String,
        format: String,
        inputExt: String = ""
    ): String {
        val fmtLower = format.lowercase().trim()
        val muxer = when (fmtLower) {
            "mkv" -> "matroska"
            "aac" -> "adts"
            else  -> fmtLower
        }

        val fmtHint   = if (inputExt.isNotBlank()) getInputFormatHint(inputExt) else ""
        val fmtPrefix = if (fmtHint.isNotEmpty()) "$fmtHint " else ""
        val mp4Flags  = if (muxer == "mp4" || muxer == "mov") " ${getMp4OutputFlags()}" else ""

        val isAudioTarget = isAudioFormat(fmtLower)

        return if (isAudioTarget) {
            // Output format is AUDIO (mp3, aac, etc.): disable video stream (-vn) and use audio encoder
            when (fmtLower) {
                "mp3" -> "-y ${fmtPrefix}-i \"$input\" -vn -c:a libmp3lame -q:a 2 -f mp3 \"$output\""
                "aac" -> "-y ${fmtPrefix}-i \"$input\" -vn -c:a aac -b:a 192k -f adts \"$output\""
                "wav" -> "-y ${fmtPrefix}-i \"$input\" -vn -c:a pcm_s16le -f wav \"$output\""
                "flac" -> "-y ${fmtPrefix}-i \"$input\" -vn -c:a flac -f flac \"$output\""
                "ogg" -> "-y ${fmtPrefix}-i \"$input\" -vn -c:a libvorbis -q:a 4 -f ogg \"$output\""
                "m4a" -> "-y ${fmtPrefix}-i \"$input\" -vn -c:a aac -b:a 192k -f mp4 \"$output\""
                else  -> "-y ${fmtPrefix}-i \"$input\" -vn -c:a aac -b:a 192k -f $muxer \"$output\""
            }
        } else {
            // Output format is VIDEO (mp4, mkv, mov, avi, webm, etc.)
            when (preset) {
                "Compress Video" ->
                    "-y ${fmtPrefix}-i \"$input\" -c:v libx264 -crf 28 -preset medium -c:a aac$mp4Flags -f $muxer \"$output\""
                "720p" ->
                    "-y ${fmtPrefix}-i \"$input\" -vf scale=-2:720 -c:v libx264 -crf 23 -preset medium -c:a aac$mp4Flags -f $muxer \"$output\""
                "Video to MP3" ->
                    "-y ${fmtPrefix}-i \"$input\" -vn -c:a libmp3lame -q:a 2 -f mp3 \"$output\""
                else -> // Original Quality
                    "-y ${fmtPrefix}-i \"$input\" -c:v copy -c:a copy$mp4Flags -f $muxer \"$output\""
            }
        }
    }

    suspend fun runQueue(context: Context) = withContext(Dispatchers.IO) {
        val uri = _outputFolderUri.value
        if (uri == null) {
            addLog("Error: Folder output belum dipilih.")
            _isConverting.value = false
            return@withContext
        }

        val outputDir = if (uri.scheme == "file") {
            DocumentFile.fromFile(File(uri.path!!))
        } else {
            DocumentFile.fromTreeUri(context, uri)
        }

        if (outputDir == null || !outputDir.canWrite()) {
            addLog("Error: Tidak memiliki izin untuk menulis ke folder output.")
            _isConverting.value = false
            return@withContext
        }

        _isConverting.value = true
        _totalProgress.value = 0
        _totalEta.value = ""
        _currentFileIndex.value = 0
        _logs.value = emptyList()

        val items = _queue.value
        _totalFiles.value = items.size

        for (i in items.indices) {
            // Check cancellation before processing next file
            if (!_isConverting.value) {
                addLog("Konversi dihentikan oleh pengguna.")
                break
            }

            _currentFileIndex.value = i + 1
            val item = items[i]

            // Check if item was cancelled in live queue
            val liveItemStatus = _queue.value.find { it.id == item.id }?.status
            if (liveItemStatus == QueueStatus.CANCELLED) continue

            updateItemStatus(item.id, QueueStatus.PROCESSING)
            addLog("Memulai konversi: ${item.inputFileName}")

            val tempInputFile = File(
                context.cacheDir,
                "ffmpeg_in_${UUID.randomUUID()}.${item.inputFormat}"
            )
            val tempOutputFile = File(
                context.cacheDir,
                "ffmpeg_out_${UUID.randomUUID()}.${_outputFormat.value}"
            )

            try {
                // Step 1: Copy SAF input file to cache (seekable)
                addLog("Menyalin ke cache (seekable)...")
                val inputStream = context.contentResolver
                    .openInputStream(Uri.parse(item.inputUri))
                    ?: throw Exception("Gagal membuka InputStream SAF untuk ${item.inputFileName}")

                inputStream.use { src ->
                    tempInputFile.outputStream().use { dst ->
                        val buf = ByteArray(4 * 1024 * 1024)
                        var read = src.read(buf)
                        while (read != -1) {
                            if (!_isConverting.value) throw Exception("Dibatalkan oleh pengguna")
                            dst.write(buf, 0, read)
                            read = src.read(buf)
                        }
                    }
                }
                val inputPath = tempInputFile.absolutePath
                addLog("Cache siap (${tempInputFile.length() / 1_048_576} MB). Memulai konversi...")

                if (!_isConverting.value) break

                val originalNameWithoutExt = item.inputFileName.substringBeforeLast('.')
                val outFileName = "${originalNameWithoutExt}_converted.${_outputFormat.value}"

                // Step 2: Create SAF output document file with proper MIME type
                val mimeType = getMimeType(_outputFormat.value)
                val outputDocFile = outputDir.createFile(mimeType, outFileName)
                    ?: throw Exception("Gagal membuat file output untuk ${item.inputFileName}")

                // Step 3: Build command
                val cmd = buildCommand(
                    input    = inputPath,
                    output   = tempOutputFile.absolutePath,
                    preset   = _preset.value,
                    format   = _outputFormat.value,
                    inputExt = item.inputFormat
                )
                addLog("Command: $cmd")

                // Step 4: Read media duration with FFprobe
                var durationMs = 0L
                try {
                    val probeSession = FFprobeKit.getMediaInformation(inputPath)
                    val durStr = probeSession.mediaInformation?.duration
                    if (durStr != null) {
                        durationMs = (durStr.toFloat() * 1000).toLong()
                    }
                } catch (e: Exception) {
                    addLog("Warning: Gagal membaca durasi media: ${e.message}")
                }

                val deferred = CompletableDeferred<Unit>()
                val startTime = System.currentTimeMillis()
                var lastEtaLogTime = 0L

                // Step 5: Execute FFmpeg
                FFmpegKit.executeAsync(
                    cmd,
                    { session ->
                        val returnCode = session.returnCode
                        if (ReturnCode.isSuccess(returnCode)) {
                            var copyOk = false
                            try {
                                addLog("Menyalin hasil ke folder tujuan...")
                                context.contentResolver.openOutputStream(outputDocFile.uri)?.use { dst ->
                                    tempOutputFile.inputStream().use { src ->
                                        val buf = ByteArray(4 * 1024 * 1024)
                                        var read = src.read(buf)
                                        while (read != -1) {
                                            dst.write(buf, 0, read)
                                            read = src.read(buf)
                                        }
                                    }
                                }
                                copyOk = true
                            } catch (e: Exception) {
                                addLog("Error: Gagal menyalin hasil ke SAF: ${e.message}")
                            }

                            if (copyOk) {
                                addLog("[SUCCESS] Berhasil: $outFileName")
                                updateItemStatus(item.id, QueueStatus.SUCCESS)
                                updateItemProgress(item.id, 100)

                                if (_deleteOriginal.value) {
                                    try {
                                        val inputDoc = DocumentFile.fromSingleUri(context, Uri.parse(item.inputUri))
                                        if (inputDoc != null && inputDoc.delete()) {
                                            addLog("File asli ${item.inputFileName} berhasil dihapus.")
                                        } else {
                                            addLog("Gagal menghapus file asli ${item.inputFileName}.")
                                        }
                                    } catch (e: Exception) {
                                        addLog("Error menghapus file asli: ${e.message}")
                                    }
                                }

                                scope.launch(Dispatchers.IO) {
                                    AppDatabase.getDatabase(context).historyDao().insert(
                                        HistoryEntity(
                                            id = UUID.randomUUID().toString(),
                                            inputFileName = item.inputFileName,
                                            outputFileName = outFileName,
                                            outputFormat = _outputFormat.value,
                                            status = "SUCCESS",
                                            durationMs = durationMs,
                                            ffmpegVersion = FFmpegKitConfig.getFFmpegVersion(),
                                            commandLine = cmd
                                        )
                                    )
                                }
                            } else {
                                updateItemStatus(item.id, QueueStatus.FAILED)
                                scope.launch(Dispatchers.IO) {
                                    AppDatabase.getDatabase(context).historyDao().insert(
                                        HistoryEntity(
                                            id = UUID.randomUUID().toString(),
                                            inputFileName = item.inputFileName,
                                            outputFileName = outFileName,
                                            outputFormat = _outputFormat.value,
                                            status = "FAILED",
                                            durationMs = durationMs,
                                            ffmpegVersion = FFmpegKitConfig.getFFmpegVersion(),
                                            commandLine = cmd
                                        )
                                    )
                                }
                            }
                        } else if (ReturnCode.isCancel(returnCode)) {
                            addLog("[CANCELLED] Dibatalkan: ${item.inputFileName}")
                            updateItemStatus(item.id, QueueStatus.CANCELLED)
                        } else {
                            val errorMessage = session.failStackTrace ?: session.allLogsAsString ?: "Unknown error"
                            addLog("[FAILED] Gagal: ${item.inputFileName}. Log: $errorMessage")
                            updateItemStatus(item.id, QueueStatus.FAILED)
                            scope.launch(Dispatchers.IO) {
                                AppDatabase.getDatabase(context).historyDao().insert(
                                    HistoryEntity(
                                        id = UUID.randomUUID().toString(),
                                        inputFileName = item.inputFileName,
                                        outputFileName = outFileName,
                                        outputFormat = _outputFormat.value,
                                        status = "FAILED",
                                        durationMs = durationMs,
                                        ffmpegVersion = FFmpegKitConfig.getFFmpegVersion(),
                                        commandLine = cmd
                                    )
                                )
                            }
                        }

                        _totalProgress.value = 0
                        deferred.complete(Unit)
                    },
                    { log ->
                        if (log.level.value <= com.arthenica.ffmpegkit.Level.AV_LOG_INFO.value) {
                            val msg = log.message.trim()
                            if (msg.isNotEmpty()) {
                                addLog("[FFmpeg] $msg")
                            }
                        }
                    },
                    { statistics ->
                        if (durationMs > 0) {
                            val progress = ((statistics.time.toFloat() / durationMs) * 100).toInt().coerceIn(0, 100)
                            updateItemProgress(item.id, progress)
                            _totalProgress.value = progress

                            val currentTime = System.currentTimeMillis()
                            val elapsedMs = currentTime - startTime
                            val processedMs = statistics.time
                            if (processedMs > 0) {
                                val remainingMs = durationMs - processedMs
                                val speed = processedMs.toFloat() / elapsedMs.toFloat()
                                if (speed > 0) {
                                    val etaMs = (remainingMs / speed).toLong()
                                    val etaSeconds = etaMs / 1000
                                    val mins = etaSeconds / 60
                                    val secs = etaSeconds % 60
                                    val etaStr = "$mins menit $secs detik"
                                    _totalEta.value = etaStr

                                    if (currentTime - lastEtaLogTime > 5000) {
                                        addLog("Progress: $progress% | Estimasi selesai: $etaStr")
                                        lastEtaLogTime = currentTime
                                    }
                                }
                            }
                        }
                    }
                )

                deferred.await()
            } catch (e: Exception) {
                addLog("Error saat memproses ${item.inputFileName}: ${e.message}")
                updateItemStatus(item.id, QueueStatus.FAILED)
            } finally {
                // ALWAYS clean up temp cache files in finally block to prevent storage leaks
                try {
                    if (tempInputFile.exists()) tempInputFile.delete()
                } catch (_: Exception) {}
                try {
                    if (tempOutputFile.exists()) tempOutputFile.delete()
                } catch (_: Exception) {}
            }

            // Check cancellation after file finishes
            if (!_isConverting.value) {
                addLog("Sisa antrean dibatalkan.")
                break
            }
        }

        _isConverting.value = false
    }
}
