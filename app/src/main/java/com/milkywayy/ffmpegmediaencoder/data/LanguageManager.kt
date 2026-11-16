package com.milkywayy.ffmpegmediaencoder.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import java.util.Locale

data class LanguageOption(
    val code: String,
    val label: String,
    val shortCode: String,
    val dir: String
)

data class AppStrings(
    val appTitle: String,
    val navConverter: String,
    val navLog: String,
    val navHistory: String,
    val topBarConverter: String,
    val topBarLog: String,
    val topBarHistory: String,
    val queueTitle: String,
    val selectAll: String,
    val deselectAll: String,
    val deleteSelected: String,
    val emptyQueueTitle: String,
    val emptyQueueDesc: String,
    val addFiles: String,
    val conversionSettings: String,
    val formatLabel: String,
    val presetLabel: String,
    val selectOutputFolder: String,
    val outputFolderPrefix: String,
    val deleteOriginalCheck: String,
    val confirmTitle: String,
    val confirmDeleteOriginalMsg: String,
    val yesDelete: String,
    val cancel: String,
    val startConversion: String,
    val converting: String,
    val statusWaiting: String,
    val statusProcessing: String,
    val statusSuccess: String,
    val statusFailed: String,
    val statusCancelled: String,
    val fileOfTotal: String,
    val etaPrefix: String,
    val terminalLogHeader: String,
    val copyLog: String,
    val logCopied: String,
    val waitingCommand: String,
    val cancelConversion: String,
    val historyTitle: String,
    val itemsCount: String,
    val selectedCount: String,
    val deleteHistoryTitle: String,
    val deleteAllHistoryConfirm: String,
    val deleteSelectedHistoryConfirm: String,
    val emptyHistoryTitle: String,
    val emptyHistoryDesc: String,
    val detail: String,
    val delete: String,
    val historySuccess: String,
    val historyFailed: String
)

val LocalStrings = staticCompositionLocalOf {
    LanguageManager.getStrings("auto")
}

object LanguageManager {
    private const val PREFS_NAME = "EncoderPrefs"
    private const val KEY_LANGUAGE = "selected_language_code"
    const val DEFAULT_LANGUAGE_CODE = "auto"

    val supportedLanguages: List<LanguageOption> = listOf(
        LanguageOption(code = "auto", label = "Auto Detect (System)", shortCode = "AUTO", dir = "auto"),
        LanguageOption(code = "id", label = "Bahasa Indonesia", shortCode = "ID", dir = "ltr"),
        LanguageOption(code = "en-US", label = "English (US)", shortCode = "EN", dir = "ltr"),
        LanguageOption(code = "en-GB", label = "English (UK)", shortCode = "UK", dir = "ltr"),
        LanguageOption(code = "ms", label = "Bahasa Melayu", shortCode = "MS", dir = "ltr"),
        LanguageOption(code = "ar", label = "العربية", shortCode = "AR", dir = "rtl"),
        LanguageOption(code = "zh-CN", label = "中文 (简体)", shortCode = "ZH", dir = "ltr"),
        LanguageOption(code = "zh-TW", label = "中文 (繁體)", shortCode = "TW", dir = "ltr"),
        LanguageOption(code = "ja", label = "日本語", shortCode = "JA", dir = "ltr"),
        LanguageOption(code = "ko", label = "한국어", shortCode = "KO", dir = "ltr"),
        LanguageOption(code = "es", label = "Español", shortCode = "ES", dir = "ltr"),
        LanguageOption(code = "fr", label = "Français", shortCode = "FR", dir = "ltr"),
        LanguageOption(code = "de", label = "Deutsch", shortCode = "DE", dir = "ltr"),
        LanguageOption(code = "pt", label = "Português", shortCode = "PT", dir = "ltr"),
        LanguageOption(code = "ru", label = "Русский", shortCode = "RU", dir = "ltr"),
        LanguageOption(code = "hi", label = "हिन्दी", shortCode = "HI", dir = "ltr"),
        LanguageOption(code = "th", label = "ไทย", shortCode = "TH", dir = "ltr"),
        LanguageOption(code = "vi", label = "Tiếng Việt", shortCode = "VI", dir = "ltr")
    )

    fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSavedLanguageCode(context: Context): String {
        return getPreferences(context).getString(KEY_LANGUAGE, DEFAULT_LANGUAGE_CODE) ?: DEFAULT_LANGUAGE_CODE
    }

    fun saveLanguageCode(context: Context, code: String) {
        getPreferences(context).edit().putString(KEY_LANGUAGE, code).apply()
    }

    fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = Resources.getSystem().configuration.locales
            if (!locales.isEmpty) locales[0] else Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale ?: Locale.getDefault()
        }
    }

    fun detectSystemLanguage(): LanguageOption {
        val systemLocale = getSystemLocale()
        val rawLang = systemLocale.language.lowercase(Locale.ROOT)
        val normalizedLang = when (rawLang) {
            "in" -> "id"
            "iw" -> "he"
            "ji" -> "yi"
            else -> rawLang
        }
        val country = systemLocale.country.uppercase(Locale.ROOT)
        val script = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            systemLocale.script.lowercase(Locale.ROOT)
        } else ""

        val fullTag = "$normalizedLang-$country"
        val fullMatch = supportedLanguages.find {
            it.code != "auto" && it.code.equals(fullTag, ignoreCase = true)
        }
        if (fullMatch != null) return fullMatch

        if (normalizedLang == "zh") {
            if (script == "hant" || country in listOf("TW", "HK", "MO")) {
                return supportedLanguages.first { it.code == "zh-TW" }
            }
            return supportedLanguages.first { it.code == "zh-CN" }
        }

        if (normalizedLang == "en") {
            if (country == "GB" || country in listOf("UK", "AU", "NZ")) {
                return supportedLanguages.first { it.code == "en-GB" }
            }
            return supportedLanguages.first { it.code == "en-US" }
        }

        val langMatch = supportedLanguages.find {
            it.code != "auto" && it.code.equals(normalizedLang, ignoreCase = true)
        }
        if (langMatch != null) return langMatch

        if (normalizedLang == "id" || country == "ID") {
            return supportedLanguages.first { it.code == "id" }
        }

        return supportedLanguages.first { it.code == "en-US" }
    }

    fun resolveEffectiveLanguage(code: String): LanguageOption {
        if (code == "auto") {
            val detected = detectSystemLanguage()
            val isRtl = detected.dir == "rtl" ||
                TextUtilsCompat.getLayoutDirectionFromLocale(getSystemLocale()) == ViewCompat.LAYOUT_DIRECTION_RTL
            return detected.copy(dir = if (isRtl) "rtl" else "ltr")
        }

        val found = supportedLanguages.find { it.code.equals(code, ignoreCase = true) }
        return found ?: supportedLanguages.first { it.code == "en-US" }
    }

    fun getOption(code: String): LanguageOption {
        return supportedLanguages.find { it.code.equals(code, ignoreCase = true) }
            ?: supportedLanguages.first()
    }

    // -------------------------------------------------------------
    // Translation Tables (i18n)
    // -------------------------------------------------------------

    private val stringsEnglish = AppStrings(
        appTitle = "FFmpeg Media Encoder",
        navConverter = "Converter",
        navLog = "Terminal Log",
        navHistory = "History",
        topBarConverter = "FFmpeg Media Encoder",
        topBarLog = "Terminal Log",
        topBarHistory = "Conversion History",
        queueTitle = "Queue (%d)",
        selectAll = "Select All",
        deselectAll = "Deselect All",
        deleteSelected = "Delete Selected",
        emptyQueueTitle = "Queue is empty",
        emptyQueueDesc = "Tap + Add Files to start converting media.",
        addFiles = "Add Files",
        conversionSettings = "Conversion Settings",
        formatLabel = "Format",
        presetLabel = "Preset",
        selectOutputFolder = "Select Output Folder",
        outputFolderPrefix = "Output: ",
        deleteOriginalCheck = "Delete original file after conversion",
        confirmTitle = "Confirmation",
        confirmDeleteOriginalMsg = "Are you sure you want to delete original file after successful conversion? This cannot be undone.",
        yesDelete = "Yes, Delete",
        cancel = "Cancel",
        startConversion = "Start Conversion",
        converting = "Converting...",
        statusWaiting = "Waiting",
        statusProcessing = "Processing",
        statusSuccess = "Completed",
        statusFailed = "Failed",
        statusCancelled = "Cancelled",
        fileOfTotal = "File %1\$d of %2\$d",
        etaPrefix = "ETA: %s",
        terminalLogHeader = "Terminal Log",
        copyLog = "Copy log",
        logCopied = "Log copied to clipboard!",
        waitingCommand = "> Waiting for command...",
        cancelConversion = "Cancel",
        historyTitle = "Conversion History",
        itemsCount = "%d items",
        selectedCount = "(%d selected)",
        deleteHistoryTitle = "Delete History",
        deleteAllHistoryConfirm = "Delete all conversion history (%d items)? This action cannot be undone.",
        deleteSelectedHistoryConfirm = "Delete %d selected history items? This action cannot be undone.",
        emptyHistoryTitle = "No history yet",
        emptyHistoryDesc = "Conversion history will appear here",
        detail = "Details",
        delete = "Delete",
        historySuccess = "Success",
        historyFailed = "Failed"
    )

    private val stringsIndonesian = AppStrings(
        appTitle = "FFmpeg Media Encoder",
        navConverter = "Converter",
        navLog = "Terminal Log",
        navHistory = "Riwayat",
        topBarConverter = "FFmpeg Media Encoder",
        topBarLog = "Terminal Log",
        topBarHistory = "Riwayat Konversi",
        queueTitle = "Antrian (%d)",
        selectAll = "Pilih Semua",
        deselectAll = "Batal Pilih Semua",
        deleteSelected = "Hapus Terpilih",
        emptyQueueTitle = "Tidak ada antrian",
        emptyQueueDesc = "Ketuk tombol + Tambah File untuk memulai proses konversi.",
        addFiles = "Tambah File",
        conversionSettings = "Pengaturan Konversi",
        formatLabel = "Format",
        presetLabel = "Preset",
        selectOutputFolder = "Pilih Folder Output",
        outputFolderPrefix = "Output: ",
        deleteOriginalCheck = "Hapus file asli setelah konversi",
        confirmTitle = "Konfirmasi",
        confirmDeleteOriginalMsg = "Apakah Anda yakin ingin menghapus file asli setelah konversi berhasil? File tidak dapat dikembalikan.",
        yesDelete = "Ya, Hapus",
        cancel = "Batal",
        startConversion = "Mulai Konversi",
        converting = "Mengonversi...",
        statusWaiting = "Menunggu",
        statusProcessing = "Proses",
        statusSuccess = "Selesai",
        statusFailed = "Gagal",
        statusCancelled = "Dibatalkan",
        fileOfTotal = "File %1\$d dari %2\$d",
        etaPrefix = "Estimasi: %s",
        terminalLogHeader = "Terminal Log",
        copyLog = "Salin log",
        logCopied = "Log tersalin ke clipboard!",
        waitingCommand = "> Menunggu perintah...",
        cancelConversion = "Batalkan",
        historyTitle = "Riwayat Konversi",
        itemsCount = "%d item",
        selectedCount = "(%d dipilih)",
        deleteHistoryTitle = "Hapus Riwayat",
        deleteAllHistoryConfirm = "Hapus semua riwayat konversi (%d item)? Tindakan ini tidak dapat dibatalkan.",
        deleteSelectedHistoryConfirm = "Hapus %d riwayat yang dipilih? Tindakan ini tidak dapat dibatalkan.",
        emptyHistoryTitle = "Belum ada riwayat",
        emptyHistoryDesc = "Riwayat konversi akan muncul di sini",
        detail = "Detail",
        delete = "Hapus",
        historySuccess = "Berhasil",
        historyFailed = "Gagal"
    )

    private val stringsArabic = AppStrings(
        appTitle = "FFmpeg Media Encoder",
        navConverter = "المحول",
        navLog = "سجل الأوامر",
        navHistory = "السجل",
        topBarConverter = "FFmpeg Media Encoder",
        topBarLog = "سجل الأوامر",
        topBarHistory = "سجل التحويل",
        queueTitle = "قائمة الانتظار (%d)",
        selectAll = "تحديد الكل",
        deselectAll = "إلغاء تحديد الكل",
        deleteSelected = "حذف المحدد",
        emptyQueueTitle = "قائمة الانتظار فارغة",
        emptyQueueDesc = "انقر فوق + إضافة ملفات لبدء تحويل الوسائط.",
        addFiles = "إضافة ملفات",
        conversionSettings = "إعدادات التحويل",
        formatLabel = "الصيغة",
        presetLabel = "الإعداد المسبق",
        selectOutputFolder = "تحديد مجلد الإخراج",
        outputFolderPrefix = "الإخراج: ",
        deleteOriginalCheck = "حذف الملف الأصلي بعد التحويل",
        confirmTitle = "تأكيد",
        confirmDeleteOriginalMsg = "هل أنت متأكد من حذف الملف الأصلي بعد نجاح التحويل؟ لا يمكن التراجع عن هذا الإجراء.",
        yesDelete = "نعم، احذف",
        cancel = "إلغاء",
        startConversion = "بدء التحويل",
        converting = "جارٍ التحويل...",
        statusWaiting = "في الانتظار",
        statusProcessing = "قيد المعالجة",
        statusSuccess = "مكتمل",
        statusFailed = "فشل",
        statusCancelled = "ملغى",
        fileOfTotal = "الملف %1\$d من %2\$d",
        etaPrefix = "الوقت المتبقي: %s",
        terminalLogHeader = "سجل الأوامر",
        copyLog = "نسخ السجل",
        logCopied = "تم نسخ السجل إلى الحافظة!",
        waitingCommand = "> في انتظار الأوامر...",
        cancelConversion = "إلغاء",
        historyTitle = "سجل التحويل",
        itemsCount = "%d عنصر",
        selectedCount = "(تم تحديد %d)",
        deleteHistoryTitle = "حذف السجل",
        deleteAllHistoryConfirm = "هل تريد حذف كل سجل التحويل (%d عنصر)؟ لا يمكن التراجع.",
        deleteSelectedHistoryConfirm = "هل تريد حذف %d عنصر محدد من السجل؟",
        emptyHistoryTitle = "لا يوجد سجل حتى الآن",
        emptyHistoryDesc = "ستظهر سجلات التحويل هنا",
        detail = "التفاصيل",
        delete = "حذف",
        historySuccess = "ناجح",
        historyFailed = "فشل"
    )

    private val stringsMalay = stringsIndonesian.copy(
        navHistory = "Sejarah",
        topBarHistory = "Sejarah Penukaran",
        queueTitle = "Giliran (%d)",
        emptyQueueTitle = "Tiada giliran",
        emptyQueueDesc = "Ketik + Tambah Fail untuk memulakan penukaran.",
        addFiles = "Tambah Fail",
        conversionSettings = "Tetapan Penukaran",
        selectOutputFolder = "Pilih Folder Output",
        deleteOriginalCheck = "Padam fail asal selepas penukaran",
        confirmDeleteOriginalMsg = "Adakah anda pasti mahu memadam fail asal selepas penukaran berjaya? Tindakan ini tidak boleh diundur.",
        yesDelete = "Ya, Padam",
        startConversion = "Mula Menukar",
        converting = "Menukar...",
        statusSuccess = "Selesai",
        historyTitle = "Sejarah Penukaran",
        deleteHistoryTitle = "Padam Sejarah",
        emptyHistoryTitle = "Tiada sejarah lagi",
        emptyHistoryDesc = "Sejarah penukaran akan dipaparkan di sini",
        delete = "Padam"
    )

    private val stringsChineseSimp = AppStrings(
        appTitle = "FFmpeg Media Encoder",
        navConverter = "转换器",
        navLog = "终端日志",
        navHistory = "历史记录",
        topBarConverter = "FFmpeg Media Encoder",
        topBarLog = "终端日志",
        topBarHistory = "转换历史",
        queueTitle = "队列 (%d)",
        selectAll = "全选",
        deselectAll = "取消全选",
        deleteSelected = "删除选中项",
        emptyQueueTitle = "队列为空",
        emptyQueueDesc = "点击 + 添加文件开始转换媒体。",
        addFiles = "添加文件",
        conversionSettings = "转换设置",
        formatLabel = "格式",
        presetLabel = "预设",
        selectOutputFolder = "选择输出文件夹",
        outputFolderPrefix = "输出: ",
        deleteOriginalCheck = "转换完成后删除原文件",
        confirmTitle = "确认",
        confirmDeleteOriginalMsg = "转换成功后确定要删除原文件吗？此操作无法撤销。",
        yesDelete = "确定删除",
        cancel = "取消",
        startConversion = "开始转换",
        converting = "正在转换...",
        statusWaiting = "等待中",
        statusProcessing = "处理中",
        statusSuccess = "已完成",
        statusFailed = "失败",
        statusCancelled = "已取消",
        fileOfTotal = "文件 %1\$d / %2\$d",
        etaPrefix = "预计剩余时间: %s",
        terminalLogHeader = "终端日志",
        copyLog = "复制日志",
        logCopied = "日志已复制到剪贴板！",
        waitingCommand = "> 等待指令...",
        cancelConversion = "取消",
        historyTitle = "转换历史",
        itemsCount = "%d 项",
        selectedCount = "(已选 %d 项)",
        deleteHistoryTitle = "删除历史",
        deleteAllHistoryConfirm = "删除所有转换历史记录 (%d 项) 吗？此操作不可逆。",
        deleteSelectedHistoryConfirm = "确定要删除选中的 %d 项历史记录吗？",
        emptyHistoryTitle = "暂无历史记录",
        emptyHistoryDesc = "转换历史记录将在此处显示",
        detail = "详情",
        delete = "删除",
        historySuccess = "成功",
        historyFailed = "失败"
    )

    private val stringsChineseTrad = stringsChineseSimp.copy(
        navConverter = "轉換器",
        navLog = "終端日誌",
        navHistory = "歷史記錄",
        topBarLog = "終端日誌",
        topBarHistory = "轉換歷史",
        queueTitle = "隊列 (%d)",
        selectAll = "全選",
        deselectAll = "取消全選",
        deleteSelected = "刪除選取項",
        emptyQueueTitle = "隊列為空",
        emptyQueueDesc = "點擊 + 新增檔案開始轉換媒體。",
        addFiles = "新增檔案",
        conversionSettings = "轉換設定",
        presetLabel = "預設",
        selectOutputFolder = "選擇輸出資料夾",
        deleteOriginalCheck = "轉換完成後刪除原檔案",
        confirmTitle = "確認",
        confirmDeleteOriginalMsg = "轉換成功後確定要刪除原檔案嗎？此操作無法復原。",
        startConversion = "開始轉換",
        converting = "正在轉換...",
        fileOfTotal = "檔案 %1\$d / %2\$d",
        etaPrefix = "預計剩餘時間: %s",
        terminalLogHeader = "終端日誌",
        copyLog = "複製日誌",
        logCopied = "日誌已複製到剪貼簿！",
        waitingCommand = "> 等待指令...",
        deleteHistoryTitle = "刪除歷史",
        deleteAllHistoryConfirm = "刪除所有轉換歷史記錄 (%d 項) 嗎？此操作不可逆。",
        deleteSelectedHistoryConfirm = "確定要刪除選中的 %d 項歷史記錄嗎？",
        emptyHistoryTitle = "尚無歷史記錄",
        emptyHistoryDesc = "轉換歷史記錄將在此處顯示",
        detail = "詳情"
    )

    private val stringsJapanese = AppStrings(
        appTitle = "FFmpeg Media Encoder",
        navConverter = "変換",
        navLog = "ログ",
        navHistory = "履歴",
        topBarConverter = "FFmpeg Media Encoder",
        topBarLog = "ターミナルログ",
        topBarHistory = "変換履歴",
        queueTitle = "キュー (%d)",
        selectAll = "すべて選択",
        deselectAll = "選択解除",
        deleteSelected = "選択項目を削除",
        emptyQueueTitle = "キューが空です",
        emptyQueueDesc = "+ ファイルを追加をタップして変換を開始します。",
        addFiles = "ファイルを追加",
        conversionSettings = "変換設定",
        formatLabel = "フォーマット",
        presetLabel = "プリセット",
        selectOutputFolder = "出力先フォルダを選択",
        outputFolderPrefix = "出力: ",
        deleteOriginalCheck = "変換完了後に元ファイルを削除",
        confirmTitle = "確認",
        confirmDeleteOriginalMsg = "変換成功後に元ファイルを削除してもよろしいですか？この操作は元に戻せません。",
        yesDelete = "削除する",
        cancel = "キャンセル",
        startConversion = "変換を開始",
        converting = "変換中...",
        statusWaiting = "待機中",
        statusProcessing = "処理中",
        statusSuccess = "完了",
        statusFailed = "失敗",
        statusCancelled = "中止",
        fileOfTotal = "ファイル %1\$d / %2\$d",
        etaPrefix = "残り時間: %s",
        terminalLogHeader = "ターミナルログ",
        copyLog = "ログをコピー",
        logCopied = "ログをクリップボードにコピーしました！",
        waitingCommand = "> コマンド待機中...",
        cancelConversion = "キャンセル",
        historyTitle = "変換履歴",
        itemsCount = "%d 件",
        selectedCount = "(%d 件選択)",
        deleteHistoryTitle = "履歴を削除",
        deleteAllHistoryConfirm = "すべての変換履歴 (%d 件) を削除しますか？",
        deleteSelectedHistoryConfirm = "選択した %d 件の履歴を削除しますか？",
        emptyHistoryTitle = "履歴はありません",
        emptyHistoryDesc = "変換履歴がここに表示されます",
        detail = "詳細",
        delete = "削除",
        historySuccess = "成功",
        historyFailed = "失敗"
    )

    private val stringsKorean = AppStrings(
        appTitle = "FFmpeg Media Encoder",
        navConverter = "변환기",
        navLog = "로그",
        navHistory = "기록",
        topBarConverter = "FFmpeg Media Encoder",
        topBarLog = "터미널 로그",
        topBarHistory = "변환 기록",
        queueTitle = "대기열 (%d)",
        selectAll = "전체 선택",
        deselectAll = "선택 해제",
        deleteSelected = "선택 항목 삭제",
        emptyQueueTitle = "대기열이 비어 있습니다",
        emptyQueueDesc = "+ 파일 추가를 눌러 변환을 시작하세요.",
        addFiles = "파일 추가",
        conversionSettings = "변환 설정",
        formatLabel = "포맷",
        presetLabel = "프리셋",
        selectOutputFolder = "출력 폴더 선택",
        outputFolderPrefix = "출력: ",
        deleteOriginalCheck = "변환 완료 후 원본 파일 삭제",
        confirmTitle = "확인",
        confirmDeleteOriginalMsg = "변환 완료 후 원본 파일을 삭제하시겠습니까? 되돌릴 수 없습니다.",
        yesDelete = "삭제",
        cancel = "취소",
        startConversion = "변환 시작",
        converting = "변환 중...",
        statusWaiting = "대기 중",
        statusProcessing = "처리 중",
        statusSuccess = "완료",
        statusFailed = "실패",
        statusCancelled = "취소됨",
        fileOfTotal = "파일 %1\$d / %2\$d",
        etaPrefix = "예상 시간: %s",
        terminalLogHeader = "터미널 로그",
        copyLog = "로그 복사",
        logCopied = "로그가 클립보드에 복사되었습니다!",
        waitingCommand = "> 명령 대기 중...",
        cancelConversion = "취소",
        historyTitle = "변환 기록",
        itemsCount = "%d 개",
        selectedCount = "(%d 개 선택됨)",
        deleteHistoryTitle = "기록 삭제",
        deleteAllHistoryConfirm = "모든 변환 기록(%d개)을 삭제하시겠습니까?",
        deleteSelectedHistoryConfirm = "선택한 %d개의 기록을 삭제하시겠습니까?",
        emptyHistoryTitle = "기록 없음",
        emptyHistoryDesc = "변환 기록이 여기에 표시됩니다",
        detail = "상세",
        delete = "삭제",
        historySuccess = "성공",
        historyFailed = "실패"
    )

    private val stringsSpanish = stringsEnglish.copy(
        navConverter = "Convertidor",
        navHistory = "Historial",
        topBarHistory = "Historial de Conversión",
        queueTitle = "Cola (%d)",
        selectAll = "Seleccionar todo",
        deselectAll = "Deseleccionar todo",
        deleteSelected = "Eliminar seleccionados",
        emptyQueueTitle = "La cola está vacía",
        emptyQueueDesc = "Toque + Agregar archivos para comenzar.",
        addFiles = "Agregar archivos",
        conversionSettings = "Ajustes de Conversión",
        selectOutputFolder = "Seleccionar carpeta de salida",
        deleteOriginalCheck = "Eliminar archivo original tras convertir",
        confirmTitle = "Confirmación",
        confirmDeleteOriginalMsg = "¿Está seguro de eliminar el archivo original tras la conversión? Esta acción no se puede deshacer.",
        yesDelete = "Sí, eliminar",
        cancel = "Cancelar",
        startConversion = "Iniciar conversión",
        converting = "Convirtiendo...",
        statusWaiting = "En espera",
        statusProcessing = "Procesando",
        statusSuccess = "Completado",
        statusFailed = "Fallido",
        statusCancelled = "Cancelado",
        fileOfTotal = "Archivo %1\$d de %2\$d",
        etaPrefix = "Tiempo estimado: %s",
        copyLog = "Copiar registro",
        logCopied = "¡Registro copiado al portapapeles!",
        waitingCommand = "> Esperando comando...",
        cancelConversion = "Cancelar",
        historyTitle = "Historial de Conversión",
        itemsCount = "%d elementos",
        selectedCount = "(%d seleccionados)",
        deleteHistoryTitle = "Eliminar historial",
        deleteAllHistoryConfirm = "¿Eliminar todo el historial (%d elementos)?",
        deleteSelectedHistoryConfirm = "¿Eliminar los %d elementos seleccionados?",
        emptyHistoryTitle = "Aún no hay historial",
        emptyHistoryDesc = "El historial de conversión aparecerá aquí",
        detail = "Detalles",
        delete = "Eliminar",
        historySuccess = "Éxito",
        historyFailed = "Fallo"
    )

    private val stringsFrench = stringsEnglish.copy(
        navConverter = "Convertisseur",
        navHistory = "Historique",
        topBarHistory = "Historique de conversion",
        queueTitle = "File d'attente (%d)",
        selectAll = "Tout sélectionner",
        deselectAll = "Tout désélectionner",
        deleteSelected = "Supprimer la sélection",
        emptyQueueTitle = "File d'attente vide",
        emptyQueueDesc = "Appuyez sur + Ajouter pour commencer.",
        addFiles = "Ajouter des fichiers",
        conversionSettings = "Paramètres de conversion",
        selectOutputFolder = "Dossier de sortie",
        deleteOriginalCheck = "Supprimer le fichier d'origine après conversion",
        confirmTitle = "Confirmation",
        confirmDeleteOriginalMsg = "Voulez-vous vraiment supprimer le fichier d'origine ? Cette action est irréversible.",
        yesDelete = "Oui, supprimer",
        cancel = "Annuler",
        startConversion = "Démarrer la conversion",
        converting = "Conversion en cours...",
        statusWaiting = "En attente",
        statusProcessing = "Traitement",
        statusSuccess = "Terminé",
        statusFailed = "Échec",
        statusCancelled = "Annulé",
        fileOfTotal = "Fichier %1\$d sur %2\$d",
        etaPrefix = "Estimation: %s",
        copyLog = "Copier le journal",
        logCopied = "Journal copié dans le presse-papiers !",
        waitingCommand = "> En attente d'une commande...",
        cancelConversion = "Annuler",
        historyTitle = "Historique de conversion",
        itemsCount = "%d éléments",
        selectedCount = "(%d sélectionnés)",
        deleteHistoryTitle = "Supprimer l'historique",
        deleteAllHistoryConfirm = "Supprimer tout l'historique (%d éléments) ?",
        deleteSelectedHistoryConfirm = "Supprimer les %d éléments sélectionnés ?",
        emptyHistoryTitle = "Aucun historique",
        emptyHistoryDesc = "L'historique des conversions apparaîtra ici",
        detail = "Détails",
        delete = "Supprimer",
        historySuccess = "Succès",
        historyFailed = "Échec"
    )

    private val stringsGerman = stringsEnglish.copy(
        navConverter = "Konverter",
        navHistory = "Verlauf",
        topBarHistory = "Konvertierungsverlauf",
        queueTitle = "Warteschlange (%d)",
        selectAll = "Alles auswählen",
        deselectAll = "Auswahl aufheben",
        deleteSelected = "Ausgewählte löschen",
        emptyQueueTitle = "Warteschlange ist leer",
        emptyQueueDesc = "+ Dateien hinzufügen tippen, um zu beginnen.",
        addFiles = "Dateien hinzufügen",
        conversionSettings = "Konvertierungseinstellungen",
        selectOutputFolder = "Ausgabeordner wählen",
        deleteOriginalCheck = "Originaldatei nach Konvertierung löschen",
        confirmTitle = "Bestätigung",
        confirmDeleteOriginalMsg = "Möchten Sie die Originaldatei wirklich löschen? Dies kann nicht rückgängig gemacht werden.",
        yesDelete = "Ja, löschen",
        cancel = "Abbrechen",
        startConversion = "Konvertierung starten",
        converting = "Wird konvertiert...",
        statusWaiting = "Warten",
        statusProcessing = "In Bearbeitung",
        statusSuccess = "Abgeschlossen",
        statusFailed = "Fehlgeschlagen",
        statusCancelled = "Abgebrochen",
        fileOfTotal = "Datei %1\$d von %2\$d",
        etaPrefix = "Verbleibend: %s",
        copyLog = "Protokoll kopieren",
        logCopied = "Protokoll in die Zwischenablage kopiert!",
        waitingCommand = "> Warte auf Befehl...",
        cancelConversion = "Abbrechen",
        historyTitle = "Konvertierungsverlauf",
        itemsCount = "%d Einträge",
        selectedCount = "(%d ausgewählt)",
        deleteHistoryTitle = "Verlauf löschen",
        deleteAllHistoryConfirm = "Gesamten Verlauf (%d Einträge) löschen?",
        deleteSelectedHistoryConfirm = "%d ausgewählte Einträge löschen?",
        emptyHistoryTitle = "Noch kein Verlauf",
        emptyHistoryDesc = "Der Verlauf wird hier angezeigt",
        detail = "Details",
        delete = "Löschen",
        historySuccess = "Erfolgreich",
        historyFailed = "Fehlgeschlagen"
    )

    private val stringsRussian = stringsEnglish.copy(
        navConverter = "Конвертер",
        navHistory = "История",
        topBarHistory = "История конвертации",
        queueTitle = "Очередь (%d)",
        selectAll = "Выбрать все",
        deselectAll = "Снять выбор",
        deleteSelected = "Удалить выбранное",
        emptyQueueTitle = "Очередь пуста",
        emptyQueueDesc = "Нажмите + Добавить файлы для начала.",
        addFiles = "Добавить файлы",
        conversionSettings = "Настройки конвертации",
        formatLabel = "Формат",
        presetLabel = "Пресет",
        selectOutputFolder = "Папка вывода",
        deleteOriginalCheck = "Удалить оригинал после конвертации",
        confirmTitle = "Подтверждение",
        confirmDeleteOriginalMsg = "Удалить исходный файл после успешной конвертации? Это действие необратимо.",
        yesDelete = "Да, удалить",
        cancel = "Отмена",
        startConversion = "Начать конвертацию",
        converting = "Конвертация...",
        statusWaiting = "Ожидание",
        statusProcessing = "Обработка",
        statusSuccess = "Завершено",
        statusFailed = "Ошибка",
        statusCancelled = "Отменено",
        fileOfTotal = "Файл %1\$d из %2\$d",
        etaPrefix = "Осталось: %s",
        copyLog = "Копировать лог",
        logCopied = "Лог скопирован в буфер обмена!",
        waitingCommand = "> Ожидание команды...",
        cancelConversion = "Отмена",
        historyTitle = "История конвертации",
        itemsCount = "%d шт.",
        selectedCount = "(выбрано: %d)",
        deleteHistoryTitle = "Очистить историю",
        deleteAllHistoryConfirm = "Удалить всю историю (%d записей)?",
        deleteSelectedHistoryConfirm = "Удалить выбранные (%d записей)?",
        emptyHistoryTitle = "История пуста",
        emptyHistoryDesc = "История конвертации появится здесь",
        detail = "Детали",
        delete = "Удалить",
        historySuccess = "Успешно",
        historyFailed = "Ошибка"
    )

    fun getStrings(code: String): AppStrings {
        val effectiveCode = if (code == "auto") {
            detectSystemLanguage().code
        } else {
            code
        }

        val baseLang = effectiveCode.substringBefore('-').lowercase(Locale.ROOT)
        return when {
            baseLang == "id" -> stringsIndonesian
            baseLang == "ms" -> stringsMalay
            baseLang == "ar" -> stringsArabic
            effectiveCode.equals("zh-TW", ignoreCase = true) -> stringsChineseTrad
            baseLang == "zh" -> stringsChineseSimp
            baseLang == "ja" -> stringsJapanese
            baseLang == "ko" -> stringsKorean
            baseLang == "es" -> stringsSpanish
            baseLang == "fr" -> stringsFrench
            baseLang == "de" -> stringsGerman
            baseLang == "ru" -> stringsRussian
            else -> stringsEnglish
        }
    }
}
