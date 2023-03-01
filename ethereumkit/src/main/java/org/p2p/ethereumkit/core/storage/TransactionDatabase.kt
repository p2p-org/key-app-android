package org.p2p.ethereumkit.core.storage

import android.content.Context
import androidx.room.*
import org.p2p.ethereumkit.api.storage.RoomTypeConverters
import org.p2p.ethereumkit.models.InternalTransaction
import org.p2p.ethereumkit.models.Transaction
import org.p2p.ethereumkit.models.TransactionSyncerState
import org.p2p.ethereumkit.models.TransactionTag

@Database(
        entities = [
            Transaction::class,
            InternalTransaction::class,
            TransactionTag::class,
            TransactionSyncerState::class
        ],
        version = 12,
        exportSchema = false
)
@TypeConverters(RoomTypeConverters::class, TransactionDatabase.TypeConverters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun transactionTagDao(): TransactionTagDao
    abstract fun transactionSyncerStateDao(): TransactionSyncerStateDao

    companion object {

        fun getInstance(context: Context, databaseName: String): TransactionDatabase {
            return Room.databaseBuilder(context, TransactionDatabase::class.java, databaseName)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
        }
    }

    class TypeConverters {
        @TypeConverter
        fun toString(list: List<String>): String {
            return list.joinToString(separator = ",")
        }

        @TypeConverter
        fun fromString(string: String): List<String> {
            return string.split(",")
        }
    }

}
