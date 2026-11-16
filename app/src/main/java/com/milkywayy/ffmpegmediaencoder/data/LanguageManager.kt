package com.milkywayy.ffmpegmediaencoder.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import java.util.Locale

data class LanguageOption(
    val code: String,
    val label: String,
    val dir: String
)

object LanguageManager {
    private const val PREFS_NAME = "EncoderPrefs"
    private const val KEY_LANGUAGE = "selected_language_code"
    const val DEFAULT_LANGUAGE_CODE = "auto"

    val supportedLanguages: List<LanguageOption> = listOf(
        LanguageOption(code = "auto", label = "Auto Detect (System)", dir = "auto"),
        LanguageOption(code = "id", label = "Bahasa Indonesia", dir = "ltr"),
        LanguageOption(code = "en-US", label = "English (US)", dir = "ltr"),
        LanguageOption(code = "en-GB", label = "English (UK)", dir = "ltr"),
        LanguageOption(code = "ms", label = "Bahasa Melayu", dir = "ltr"),
        LanguageOption(code = "ar", label = "العربية", dir = "rtl"),
        LanguageOption(code = "zh-CN", label = "中文 (简体)", dir = "ltr"),
        LanguageOption(code = "zh-TW", label = "中文 (繁體)", dir = "ltr"),
        LanguageOption(code = "ja", label = "日本語", dir = "ltr"),
        LanguageOption(code = "ko", label = "한국어", dir = "ltr"),
        LanguageOption(code = "es", label = "Español", dir = "ltr"),
        LanguageOption(code = "fr", label = "Français", dir = "ltr"),
        LanguageOption(code = "de", label = "Deutsch", dir = "ltr"),
        LanguageOption(code = "pt", label = "Português", dir = "ltr"),
        LanguageOption(code = "ru", label = "Русский", dir = "ltr"),
        LanguageOption(code = "hi", label = "हिन्दी", dir = "ltr"),
        LanguageOption(code = "th", label = "ไทย", dir = "ltr"),
        LanguageOption(code = "vi", label = "Tiếng Việt", dir = "ltr")
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

    /**
     * Detects system language and matches it with supportedLanguages (excluding 'auto').
     * If the device language is not in the list, falls back to "en-US" or "id".
     */
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

        // 1. Direct match on language-country (e.g. en-US, en-GB, zh-CN, zh-TW)
        val fullTag = "$normalizedLang-$country"
        val fullMatch = supportedLanguages.find {
            it.code != "auto" && it.code.equals(fullTag, ignoreCase = true)
        }
        if (fullMatch != null) return fullMatch

        // Chinese script / region detection
        if (normalizedLang == "zh") {
            if (script == "hant" || country in listOf("TW", "HK", "MO")) {
                return supportedLanguages.first { it.code == "zh-TW" }
            }
            return supportedLanguages.first { it.code == "zh-CN" }
        }

        // English region detection
        if (normalizedLang == "en") {
            if (country == "GB" || country in listOf("UK", "AU", "NZ")) {
                return supportedLanguages.first { it.code == "en-GB" }
            }
            return supportedLanguages.first { it.code == "en-US" }
        }

        // 2. Direct match on base language code (e.g. id, ms, ar, ja, ko, es, fr, de, pt, ru, hi, th, vi)
        val langMatch = supportedLanguages.find {
            it.code != "auto" && it.code.equals(normalizedLang, ignoreCase = true)
        }
        if (langMatch != null) return langMatch

        // 3. Fallback logic: check if Indonesian or else en-US
        if (normalizedLang == "id" || country == "ID") {
            return supportedLanguages.first { it.code == "id" }
        }

        return supportedLanguages.first { it.code == "en-US" }
    }

    /**
     * Resolves the effective language option and its layout direction.
     * When code is "auto", resolves system language and detects RTL/LTR.
     */
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
}
