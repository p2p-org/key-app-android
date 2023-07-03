package org.p2p.token.service.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.database.entity.TokenPriceEntity
import org.p2p.token.service.model.TokenServiceNetwork

@Dao
internal interface TokenPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokenPrice(entity: TokenPriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokenPrice(entities: List<TokenPriceEntity>)

    @Query("SELECT * FROM token_price_table")
    suspend fun getAllTokensPrices(): List<TokenPriceEntity>

    @Query("SELECT * FROM token_price_table")
    fun getAllTokensPricesFlow(): Flow<TokenPriceEntity>

    @Query("SELECT * FROM token_price_table WHERE token_address = :tokenAddress")
    suspend fun getTokenPriceByAddress(tokenAddress: String): TokenPriceEntity?

    @Query("SELECT * FROM token_price_table WHERE token_network_chain = :networkChain")
    suspend fun getTokensRatesByNetwork(networkChain: String): List<TokenPriceEntity>

    @Query("SELECT * FROM token_price_table WHERE token_network_chain = :networkChain ")
    fun getTokensRatesByNetworkFlow(networkChain: String): Flow<List<TokenPriceEntity>>
}
