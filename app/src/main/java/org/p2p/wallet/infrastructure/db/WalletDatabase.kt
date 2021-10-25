package org.p2p.wallet.infrastructure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.p2p.wallet.infrastructure.db.WalletDatabase.Companion.DATABASE_VERSION
import org.p2p.wallet.main.db.SessionDao
import org.p2p.wallet.main.db.SessionEntity
import org.p2p.wallet.main.db.TokenDao
import org.p2p.wallet.main.db.TokenEntity

@Database(
    entities = [
        TokenEntity::class,
        SessionEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WalletDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "p2p.wallet"
    }

    abstract fun tokenDao(): TokenDao
    abstract fun sessionDao(): SessionDao
}