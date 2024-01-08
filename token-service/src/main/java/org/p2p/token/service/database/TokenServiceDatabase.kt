package org.p2p.token.service.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.p2p.core.room.RoomConverters
import org.p2p.token.service.database.TokenServiceDatabase.Companion.DATABASE_VERSION
import org.p2p.token.service.database.entity.TokenServicePriceEntity

@Database(
    entities = [
        TokenServicePriceEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(value = [RoomConverters::class])
internal abstract class TokenServiceDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "p2p.wallet.token_service_price"
    }

    abstract fun tokenServicePriceDao(): TokenPriceDao
}
