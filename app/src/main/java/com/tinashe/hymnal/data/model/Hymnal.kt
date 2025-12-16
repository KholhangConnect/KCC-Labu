package com.tinashe.hymnal.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = "hymnals")
@Parcelize
data class Hymnal(
    @PrimaryKey
    val code: String,
    val title: String,
    val language: String
) : Parcelable {
    @Ignore
    @IgnoredOnParcel
    var offline: Boolean = true

    @Ignore
    @IgnoredOnParcel
    var selected: Boolean = false
}
