package org.p2p.token.service.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.database.entity.TokenServicePriceEntity

@Dao
internal interface TokenPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokenPrice(entity: TokenServicePriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokenPrice(entities: List<TokenServicePriceEntity>)

    @Query("SELECT * FROM token_price_table")
    suspend fun getAllTokensPrices(): List<TokenServicePriceEntity>

    @Query("SELECT * FROM token_price_table")
    fun getAllTokensPricesFlow(): Flow<TokenServicePriceEntity>

    @Query("SELECT * FROM token_price_table WHERE token_address = :tokenAddress")
    suspend fun getTokenPriceByAddress(tokenAddress: String): TokenServicePriceEntity?

    @Query("SELECT * FROM token_price_table WHERE token_network_chain = :networkChain")
    suspend fun getTokensRatesByNetwork(networkChain: String): List<TokenServicePriceEntity>

    @Query("SELECT * FROM token_price_table WHERE token_network_chain = :networkChain ")
    fun getTokensRatesByNetworkFlow(networkChain: String): Flow<List<TokenServicePriceEntity>>
}
