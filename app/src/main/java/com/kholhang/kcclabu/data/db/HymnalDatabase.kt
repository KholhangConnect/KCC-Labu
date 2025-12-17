package com.kholhang.kcclabu.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kholhang.kcclabu.data.db.dao.CollectionsDao
import com.kholhang.kcclabu.data.db.dao.HymnalsDao
import com.kholhang.kcclabu.data.db.dao.HymnsDao
import com.kholhang.kcclabu.data.model.Hymn
import com.kholhang.kcclabu.data.model.Hymnal
import com.kholhang.kcclabu.data.model.collections.CollectionHymnCrossRef
import com.kholhang.kcclabu.data.model.collections.HymnCollection

@Database(
    entities = [
        Hymnal::class,
        Hymn::class,
        HymnCollection::class,
        CollectionHymnCrossRef::class
    ],
    version = 2, exportSchema = true
)
@TypeConverters(DataTypeConverters::class)
abstract class HymnalDatabase : RoomDatabase() {

    abstract fun hymnalsDao(): HymnalsDao

    abstract fun hymnsDao(): HymnsDao

    abstract fun collectionsDao(): CollectionsDao
}
