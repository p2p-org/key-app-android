package org.p2p.wallet.newsend.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RecipientDao {

    @Transaction
    suspend fun insertOrUpdate(entities: List<RecipientEntry>) {
        entities.forEach { entity ->
            val found = findByAddress(entity.address)
            if (found != null) {
                update(
                    entity.address,
                    entity.nickname,
                    entity.dateTimestamp
                )
            } else {
                insertOrReplace(entity)
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entities: List<RecipientEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: RecipientEntry)

    @Query(
        """
            UPDATE recipient_table
            SET nickname = :nickname, date_timestamp = :dateTimestamp
            WHERE address = :address 
        """
    )
    suspend fun update(
        address: String,
        nickname: String?,
        dateTimestamp: Long
    )

    @Query("SELECT * FROM recipient_table WHERE address = :address")
    suspend fun findByAddress(address: String): RecipientEntry?

    @Query("SELECT * FROM recipient_table ORDER BY date_timestamp DESC LIMIT 10")
    suspend fun getRecipients(): List<RecipientEntry>

    @Query("DELETE FROM token_table")
    suspend fun clearAll()
}
