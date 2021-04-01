package com.wowlet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wowlet.entities.local.LocalWalletItem

@Dao
interface LocalWalletItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveWallet(item: LocalWalletItem)


    @Query("SELECT * FROM wallet_item WHERE id = :id")
    fun findWalletById(id: String): LocalWalletItem?
}