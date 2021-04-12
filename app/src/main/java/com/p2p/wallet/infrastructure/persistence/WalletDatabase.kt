package com.p2p.wallet.infrastructure.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.p2p.wallet.dashboard.db.WalletDao
import com.p2p.wallet.dashboard.model.local.LocalWalletItem
import com.p2p.wallet.infrastructure.persistence.WalletDatabase.Companion.DATABASE_VERSION

@Database(
    entities = [LocalWalletItem::class],
    version = DATABASE_VERSION,
    exportSchema = false
)
abstract class WalletDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "wallet.database"
    }

    abstract fun walletDAO(): WalletDao
}