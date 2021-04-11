package com.p2p.wowlet.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.p2p.wowlet.entities.local.LocalWalletItem

@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveWallet(item: LocalWalletItem)

    @Query("SELECT * FROM wallet_item WHERE id = :id")
    fun findWalletById(id: String): LocalWalletItem?
}