package com.kholhang.kcclabu.data.di

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.kholhang.kcclabu.data.db.HymnalDatabase
import com.kholhang.kcclabu.data.db.dao.CollectionsDao
import com.kholhang.kcclabu.data.db.dao.HymnalsDao
import com.kholhang.kcclabu.data.db.dao.HymnsDao
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Provides
    fun provideDatabase(context: Context): HymnalDatabase =
        Room.databaseBuilder(context, HymnalDatabase::class.java, "hymnal.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHymnalsDao(database: HymnalDatabase): HymnalsDao = database.hymnalsDao()

    @Provides
    fun provideHymnsDao(database: HymnalDatabase): HymnsDao = database.hymnsDao()

    @Provides
    fun provideCollectionsDao(database: HymnalDatabase): CollectionsDao = database.collectionsDao()

    @Provides
    fun provideHymnalPrefs(context: Context): HymnalPrefs = HymnalPrefsImpl(
        PreferenceManager.getDefaultSharedPreferences(context)
    )
}
