package org.p2p.wallet.newsend.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipientsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entities: List<RecipientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: RecipientEntity)

    @Query("SELECT * FROM recipient_table WHERE address = :address")
    suspend fun findByAddress(address: String): RecipientEntity?

    @Query("SELECT * FROM recipient_table ORDER BY date_timestamp DESC LIMIT 10")
    suspend fun getRecipients(): List<RecipientEntity>

    @Query("DELETE FROM recipient_table")
    suspend fun clearAll()
}
