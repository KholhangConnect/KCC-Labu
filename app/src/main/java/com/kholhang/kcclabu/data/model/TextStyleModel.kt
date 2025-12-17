package com.kholhang.kcclabu.data.model

import android.os.Parcelable
import androidx.annotation.FontRes
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.constants.UiPref
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextStyleModel(
    val pref: UiPref = UiPref.FOLLOW_SYSTEM,
    @FontRes val fontRes: Int = R.font.proxima_nova_soft_medium,
    val textSize: Float = 18f
) : Parcelable
