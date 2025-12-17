package com.kholhang.kcclabu.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.constants.UiPref
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import com.kholhang.kcclabu.utils.Helper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: HymnalPrefs
) : ViewModel() {

    private val _theme = MutableStateFlow(prefs.getUiPref())
    val theme: StateFlow<UiPref> = _theme.asStateFlow()

    private val _fontSize = MutableStateFlow(prefs.getFontSize())
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _fontRes = MutableStateFlow(prefs.getFontRes())
    val fontRes: StateFlow<Int> = _fontRes.asStateFlow()

    private val _backgroundColor = MutableStateFlow(prefs.getBackgroundColor())
    val backgroundColor: StateFlow<Int> = _backgroundColor.asStateFlow()

    private val _chorusColor = MutableStateFlow(prefs.getChorusColor())
    val chorusColor: StateFlow<Int> = _chorusColor.asStateFlow()

    private val _highlightsEnabled = MutableStateFlow(prefs.isHighlightsEnabled())
    val highlightsEnabled: StateFlow<Boolean> = _highlightsEnabled.asStateFlow()

    private val _textIndent = MutableStateFlow(prefs.getTextIndent())
    val textIndent: StateFlow<Float> = _textIndent.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(prefs.isKeepScreenOn())
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _lineSpacing = MutableStateFlow(prefs.getLineSpacing())
    val lineSpacing: StateFlow<Float> = _lineSpacing.asStateFlow()

    private val _pinchZoomEnabled = MutableStateFlow(prefs.isPinchZoomEnabled())
    val pinchZoomEnabled: StateFlow<Boolean> = _pinchZoomEnabled.asStateFlow()

    fun setTheme(theme: UiPref) {
        viewModelScope.launch {
            prefs.setUiPref(theme)
            _theme.value = theme
            Helper.switchToTheme(theme)
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            prefs.setFontSize(size)
            _fontSize.value = size
        }
    }

    fun setFontRes(fontRes: Int) {
        viewModelScope.launch {
            prefs.setFontRes(fontRes)
            _fontRes.value = fontRes
        }
    }

    fun setBackgroundColor(color: Int) {
        viewModelScope.launch {
            prefs.setBackgroundColor(color)
            _backgroundColor.value = color
        }
    }

    fun setChorusColor(color: Int) {
        viewModelScope.launch {
            prefs.setChorusColor(color)
            _chorusColor.value = color
        }
    }

    fun setHighlightsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setHighlightsEnabled(enabled)
            _highlightsEnabled.value = enabled
        }
    }

    fun setTextIndent(indent: Float) {
        viewModelScope.launch {
            prefs.setTextIndent(indent)
            _textIndent.value = indent
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setKeepScreenOn(enabled)
            _keepScreenOn.value = enabled
        }
    }

    fun setLineSpacing(spacing: Float) {
        viewModelScope.launch {
            prefs.setLineSpacing(spacing)
            _lineSpacing.value = spacing
        }
    }

    fun setPinchZoomEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setPinchZoomEnabled(enabled)
            _pinchZoomEnabled.value = enabled
        }
    }

    companion object {
        val availableFonts = listOf(
            R.font.proxima_nova_soft_regular to R.string.font_proxima_nova,
            R.font.lato to R.string.font_lato,
            R.font.roboto to R.string.font_roboto,
            R.font.andada to R.string.font_andada,
            R.font.gentium_basic to R.string.font_gentium_basic
        )

        val backgroundColors = listOf(
            0xFFFFFFFFL to R.string.color_white,
            0xFFF5F5F5L to R.string.color_light_gray,
            0xFFE8E8E8L to R.string.color_gray,
            0xFFFFF8E1L to R.string.color_warm_white,
            0xFFE3F2FDL to R.string.color_light_blue,
            0xFFF1F8E9L to R.string.color_light_green
        )

        val chorusColors = listOf(
            0xFF0B6138L to R.string.color_green,
            0xFF1976D2L to R.string.color_blue,
            0xFFD32F2FL to R.string.color_red,
            0xFFF57C00L to R.string.color_orange,
            0xFF7B1FA2L to R.string.color_purple,
            0xFF0288D1L to R.string.color_light_blue
        )
    }
}

