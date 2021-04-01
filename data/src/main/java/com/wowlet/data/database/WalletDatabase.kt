package com.wowlet.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wowlet.data.dao.LocalWalletItemDAO
import com.wowlet.entities.local.LocalWalletItem

@Database(entities = [LocalWalletItem::class], version = 1, exportSchema = false)
abstract class WalletDatabase : RoomDatabase() {

    abstract val walletDAO: LocalWalletItemDAO
}