package com.kholhang.kcclabu.initializer

import android.content.Context
import androidx.startup.Initializer
import com.kholhang.kcclabu.BuildConfig
import com.kholhang.kcclabu.extensions.CrashlyticsTree
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
