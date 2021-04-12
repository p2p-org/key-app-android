package com.p2p.wallet.dashboard.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.p2p.wallet.dashboard.model.local.LocalWalletItem

@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveWallet(item: LocalWalletItem)

    @Query("SELECT * FROM wallet_item WHERE id = :id")
    fun findWalletById(id: String): LocalWalletItem?
}