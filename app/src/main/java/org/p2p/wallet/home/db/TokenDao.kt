package org.p2p.wallet.home.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface TokenDao {

    @Transaction
    suspend fun insertOrUpdate(entities: List<TokenEntity>) {
        entities.forEach { entity ->
            val found = findByPublicKey(entity.publicKey)
            if (found != null) {
                update(entity.publicKey, entity.totalInUsd, entity.total, entity.exchangeRate, found.visibility)
            } else {
                insertOrReplace(entity)
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entities: List<TokenEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: TokenEntity)

    @Query(
        """
            UPDATE token_table
            SET price = :price, total = :total, exchange_rate = :exchangeRate, visibility = :visibility
            WHERE public_key = :publicKey
        """
    )
    suspend fun update(
        publicKey: String,
        price: BigDecimal?,
        total: BigDecimal,
        exchangeRate: String?,
        visibility: String
    )

    @Query("DELETE FROM token_table WHERE symbol = :symbol AND public_key != :solAddress")
    suspend fun removeIfExists(solAddress: String, symbol: String)

    @Query("SELECT * FROM token_table WHERE public_key = :publicKey")
    suspend fun findByPublicKey(publicKey: String): TokenEntity?

    @Query("SELECT * FROM token_table WHERE mint_address = :mintAddress")
    suspend fun findByMintAddress(mintAddress: String): TokenEntity?

    @Query("SELECT * FROM token_table")
    fun getTokensFlow(): Flow<List<TokenEntity>>

    @Query("SELECT * FROM token_table")
    suspend fun getTokens(): List<TokenEntity>

    @Query(
        """
        UPDATE token_table
        SET visibility = :visibility
        WHERE mint_address = :mintAddress
    """
    )
    suspend fun updateVisibility(mintAddress: String, visibility: String)

    @Query("DELETE FROM token_table")
    suspend fun clearAll()
}
