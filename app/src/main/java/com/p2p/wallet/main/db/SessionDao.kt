package com.p2p.wallet.main.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SessionEntity)

    @Query("SELECT * FROM session_table WHERE destination_address = :destinationAddress")
    suspend fun findByDestinationAddress(destinationAddress: String): SessionEntity?

    @Query("DELETE FROM session_table")
    suspend fun clearAll()
}