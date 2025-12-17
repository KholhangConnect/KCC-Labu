package com.kholhang.kcclabu

import android.app.Application
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import com.kholhang.kcclabu.utils.Helper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HymnalApp : Application() {

    @Inject
    lateinit var prefs: HymnalPrefs

    override fun onCreate() {
        super.onCreate()

        try {
            // Check if prefs is initialized (Hilt should inject it, but be safe)
            if (::prefs.isInitialized) {
                Helper.switchToTheme(prefs.getUiPref())
            } else {
                // If prefs is not injected yet, use default theme
                Helper.switchToTheme(com.kholhang.kcclabu.data.model.constants.UiPref.FOLLOW_SYSTEM)
            }
        } catch (e: Exception) {
            // If there's any error, use default theme
            android.util.Log.e("HymnalApp", "Error switching theme", e)
            try {
                Helper.switchToTheme(com.kholhang.kcclabu.data.model.constants.UiPref.FOLLOW_SYSTEM)
            } catch (ex: Exception) {
                android.util.Log.e("HymnalApp", "Critical error setting default theme", ex)
            }
        }
    }
}
