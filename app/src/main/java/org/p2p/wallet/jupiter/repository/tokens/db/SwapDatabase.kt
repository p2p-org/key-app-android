package org.p2p.wallet.jupiter.repository.tokens.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import java.util.concurrent.Executors
import org.p2p.core.room.RoomConverters

@Database(
    entities = [
        SwapTokenEntity::class,
        SwapTokenRouteCrossRef::class
    ],
    version = SwapDatabase.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(value = [RoomConverters::class])
abstract class SwapDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "p2p.wallet.swap_db"
        fun create(context: Context): SwapDatabase =
            Room.databaseBuilder(context, SwapDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .setQueryExecutor(Executors.newCachedThreadPool())
                .build()
    }

    abstract val swapTokensDao: SwapTokensDao
}
