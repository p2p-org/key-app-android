package org.p2p.ethereumkit.internal.spv.core.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.p2p.ethereumkit.internal.api.storage.RoomTypeConverters
import org.p2p.ethereumkit.internal.spv.models.AccountStateSpv
import org.p2p.ethereumkit.internal.spv.models.BlockHeader

@Database(entities = [BlockHeader::class, AccountStateSpv::class], version = 2, exportSchema = true)
@TypeConverters(RoomTypeConverters::class)
abstract class SpvDatabase : RoomDatabase() {

    abstract fun blockHeaderDao(): BlockHeaderDao
    abstract fun accountStateDao(): AccountStateDao

    companion object {

        fun getInstance(context: Context, databaseName: String): SpvDatabase {
            return Room.databaseBuilder(context, SpvDatabase::class.java, databaseName)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
        }
    }
}
