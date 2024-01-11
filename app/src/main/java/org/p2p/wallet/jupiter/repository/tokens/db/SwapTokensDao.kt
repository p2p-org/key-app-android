package org.p2p.wallet.jupiter.repository.tokens.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SwapTokensDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwapTokens(entities: List<SwapTokenEntity>)

    @Query("SELECT * FROM swap_tokens WHERE address = :mintAddress")
    suspend fun getSwappableTokens(mintAddress: String): List<SwapTokenEntity>

    @Query(
        """
            SELECT * 
            FROM swap_tokens
            WHERE (LOWER(symbol) LIKE :mintAddressOrSymbol OR LOWER(address) LIKE :mintAddressOrSymbol)
        """
    )
    suspend fun searchTokensInSwappable(mintAddressOrSymbol: String): List<SwapTokenEntity>

    @Query(
        """
           SELECT * FROM swap_tokens 
           WHERE LOWER(symbol) LIKE :mintAddressOrSymbol 
           OR LOWER(address) LIKE :mintAddressOrSymbol
        """
    )
    suspend fun searchTokens(mintAddressOrSymbol: String): List<SwapTokenEntity>

    @Query("SELECT * FROM swap_tokens WHERE address = :mintAddress")
    suspend fun findTokenByMint(mintAddress: String): SwapTokenEntity?

    @Query("SELECT * FROM swap_tokens WHERE LOWER(symbol) = :symbol")
    suspend fun findTokenBySymbol(symbol: String): SwapTokenEntity?

    @Query("SELECT * FROM swap_tokens WHERE address NOT IN (:userTokensMints)")
    fun findTokensExcludingMints(userTokensMints: Set<String>): List<SwapTokenEntity>

    @Query("SELECT * FROM swap_tokens WHERE address IN (:userTokensMints)")
    fun findTokensIncludingMints(userTokensMints: Set<String>): List<SwapTokenEntity>

    @Query("SELECT * from swap_tokens where address = :mintAddress")
    suspend fun getSwapToken(mintAddress: String): SwapTokenEntity?

    @Query("SELECT * from swap_tokens")
    suspend fun getAllSwapTokens(): List<SwapTokenEntity>
}
