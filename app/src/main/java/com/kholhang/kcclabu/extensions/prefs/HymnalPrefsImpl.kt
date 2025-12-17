package com.kholhang.kcclabu.extensions.prefs

import android.content.SharedPreferences
import androidx.annotation.FontRes
import androidx.core.content.edit
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.TextStyleModel
import com.kholhang.kcclabu.data.model.constants.UiPref

class HymnalPrefsImpl(private val prefs: SharedPreferences) : HymnalPrefs {

    override fun getSelectedHymnal(): String = prefs.getString(KEY_CODE, DEFAULT_CODE)!!

    override fun setSelectedHymnal(code: String) {
        prefs.edit { putString(KEY_CODE, code) }
    }

    override fun getUiPref(): UiPref {
        val value = prefs.getString(KEY_UI_PREF, null) ?: return UiPref.FOLLOW_SYSTEM
        return UiPref.fromString(value) ?: UiPref.FOLLOW_SYSTEM
    }

    override fun setUiPref(pref: UiPref) {
        prefs.edit {
            putString(KEY_UI_PREF, pref.value)
        }
    }

    override fun setFontRes(@FontRes fontRes: Int) {
        prefs.edit { putInt(KEY_FONT_STYLE, fontRes) }
    }

    override fun getFontRes(): Int = prefs.getInt(KEY_FONT_STYLE, R.font.proxima_nova_soft_regular)

    override fun setFontSize(size: Float) {
        prefs.edit { putFloat(KEY_FONT_SIZE, size) }
    }

    override fun getFontSize(): Float = prefs.getFloat(KEY_FONT_SIZE, 22f)

    override fun getTextStyleModel(): TextStyleModel = TextStyleModel(
        getUiPref(),
        getFontRes(),
        getFontSize()
    )

    override fun isHymnalPromptSeen(): Boolean = prefs.getBoolean(KEY_HYMNALS_PROMPT, false)

    override fun setHymnalPromptSeen() {
        prefs.edit {
            putBoolean(KEY_HYMNALS_PROMPT, true)
        }
    }
    
    override fun getBackgroundColor(): Int = prefs.getInt(KEY_BACKGROUND_COLOR, 0xFFFFFFFF.toInt())
    
    override fun setBackgroundColor(color: Int) {
        prefs.edit { putInt(KEY_BACKGROUND_COLOR, color) }
    }
    
    override fun getChorusColor(): Int = prefs.getInt(KEY_CHORUS_COLOR, 0xFF0B6138.toInt()) // MTLGreen
    
    override fun setChorusColor(color: Int) {
        prefs.edit { putInt(KEY_CHORUS_COLOR, color) }
    }
    
    override fun isHighlightsEnabled(): Boolean = prefs.getBoolean(KEY_HIGHLIGHTS_ENABLED, true)
    
    override fun setHighlightsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_HIGHLIGHTS_ENABLED, enabled) }
    }
    
    override fun getTextIndent(): Float = prefs.getFloat(KEY_TEXT_INDENT, 0f)
    
    override fun setTextIndent(indent: Float) {
        prefs.edit { putFloat(KEY_TEXT_INDENT, indent) }
    }
    
    override fun isKeepScreenOn(): Boolean = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false)
    
    override fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_KEEP_SCREEN_ON, enabled) }
    }
    
    override fun getLineSpacing(): Float = prefs.getFloat(KEY_LINE_SPACING, 1.2f)
    
    override fun setLineSpacing(spacing: Float) {
        prefs.edit { putFloat(KEY_LINE_SPACING, spacing) }
    }

    override fun isPinchZoomEnabled(): Boolean = prefs.getBoolean(KEY_PINCH_ZOOM_ENABLED, true)
    
    override fun setPinchZoomEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_PINCH_ZOOM_ENABLED, enabled) }
    }

    companion object {
        private const val DEFAULT_CODE = "english"

        private const val KEY_CODE = "pref:default_code"
        private const val KEY_UI_PREF = "pref:app_theme"
        private const val KEY_FONT_STYLE = "pref:font_res"
        private const val KEY_FONT_SIZE = "pref:font_size"
        private const val KEY_HYMNALS_PROMPT = "pref:hymnals_list_prompt"
        private const val KEY_BACKGROUND_COLOR = "pref:background_color"
        private const val KEY_CHORUS_COLOR = "pref:chorus_color"
        private const val KEY_HIGHLIGHTS_ENABLED = "pref:highlights_enabled"
        private const val KEY_TEXT_INDENT = "pref:text_indent"
        private const val KEY_KEEP_SCREEN_ON = "pref:keep_screen_on"
        private const val KEY_LINE_SPACING = "pref:line_spacing"
        private const val KEY_PINCH_ZOOM_ENABLED = "pref:pinch_zoom_enabled"
    }
}
