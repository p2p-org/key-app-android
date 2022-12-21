package org.p2p.wallet.newsend.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entities: List<RecipientEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: RecipientEntry)

    @Query("SELECT * FROM recipient_table WHERE address = :address")
    suspend fun findByAddress(address: String): RecipientEntry?

    @Query("SELECT * FROM recipient_table ORDER BY date_timestamp DESC LIMIT 10")
    suspend fun getRecipients(): List<RecipientEntry>

    @Query("DELETE FROM token_table")
    suspend fun clearAll()
}
