package org.p2p.token.service.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.p2p.core.room.RoomConverters
import org.p2p.token.service.database.TokenServiceDatabase.Companion.DATABASE_VERSION
import org.p2p.token.service.database.entity.TokenPriceEntity
import org.p2p.token.service.database.entity.TokenRateEntity

@Database(
    entities = [
        TokenPriceEntity::class,
        TokenRateEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
internal abstract class TokenServiceDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "p2p.wallet.token_service_price"
    }

    abstract fun tokenServicePriceDao(): TokenPriceDao
}
