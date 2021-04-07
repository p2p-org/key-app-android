package com.p2p.wowlet.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.p2p.wowlet.dao.LocalWalletItemDAO
import com.p2p.wowlet.entities.local.LocalWalletItem

@Database(entities = [LocalWalletItem::class], version = 1, exportSchema = false)
abstract class WalletDatabase : RoomDatabase() {

    abstract val walletDAO: LocalWalletItemDAO
}