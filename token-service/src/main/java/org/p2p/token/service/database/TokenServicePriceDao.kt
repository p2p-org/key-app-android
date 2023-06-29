package org.p2p.token.service.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.database.entity.TokenServicePriceEntity
import org.p2p.token.service.model.TokenServiceNetwork

@Dao
interface TokenServicePriceDao {

    @Transaction
    suspend fun insertOrUpdate(entities: List<TokenServicePriceEntity>) {
        entities.forEach { insertOrReplace(it) }
    }

    @Query("SELECT * FROM token_price_table WHERE token_address = :tokenAddress")
    suspend fun findByTokenAddress(tokenAddress: String): TokenServicePriceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: TokenServicePriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entities: List<TokenServicePriceEntity>)

    @Query("SELECT * FROM token_price_table")
    suspend fun getTokenPrices(): List<TokenServicePriceEntity>

    @Query("SELECT * FROM token_price_table WHERE token_network_chain =:networkChain")
    suspend fun getTokenPrices(networkChain: TokenServiceNetwork): List<TokenServicePriceEntity>

    @Query("SELECT * FROM token_price_table")
    suspend fun getTokenPricesFlow(): Flow<List<TokenServicePriceEntity>>

    @Query("SELECT * FROM token_price_table WHERE token_network_chain = :networkChain")
    suspend fun getTokenPricesFlow(networkChain: TokenServiceNetwork): Flow<List<TokenServicePriceEntity>>
}
