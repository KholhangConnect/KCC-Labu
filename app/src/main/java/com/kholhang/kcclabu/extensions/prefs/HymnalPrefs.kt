package com.kholhang.kcclabu.extensions.prefs

import androidx.annotation.FontRes
import com.kholhang.kcclabu.data.model.TextStyleModel
import com.kholhang.kcclabu.data.model.constants.UiPref

interface HymnalPrefs {

    fun getSelectedHymnal(): String

    fun setSelectedHymnal(code: String)

    fun getUiPref(): UiPref

    fun setUiPref(pref: UiPref)

    fun setFontRes(@FontRes fontRes: Int)

    @FontRes
    fun getFontRes(): Int

    fun setFontSize(size: Float)

    fun getFontSize(): Float

    fun getTextStyleModel(): TextStyleModel

    fun isHymnalPromptSeen(): Boolean

    fun setHymnalPromptSeen()
    
    // New settings
    fun getBackgroundColor(): Int
    fun setBackgroundColor(color: Int)
    
    fun getChorusColor(): Int
    fun setChorusColor(color: Int)
    
    fun isHighlightsEnabled(): Boolean
    fun setHighlightsEnabled(enabled: Boolean)
    
    fun getTextIndent(): Float
    fun setTextIndent(indent: Float)
    
    fun isKeepScreenOn(): Boolean
    fun setKeepScreenOn(enabled: Boolean)
    
    fun getLineSpacing(): Float
    fun setLineSpacing(spacing: Float)
    
    fun isPinchZoomEnabled(): Boolean
    fun setPinchZoomEnabled(enabled: Boolean)
}
