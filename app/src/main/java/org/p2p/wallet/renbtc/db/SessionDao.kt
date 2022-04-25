package org.p2p.wallet.renbtc.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SessionEntity)

    @Query("SELECT * FROM session_table WHERE destination_address = :destinationAddress")
    fun getSessionFlow(destinationAddress: String): Flow<SessionEntity?>

    @Query("SELECT * FROM session_table WHERE destination_address = :destinationAddress")
    suspend fun findByDestinationAddress(destinationAddress: String): SessionEntity?

    @Query("DELETE FROM session_table")
    suspend fun clearAll()
}
