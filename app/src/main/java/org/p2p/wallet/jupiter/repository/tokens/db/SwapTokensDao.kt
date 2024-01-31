package org.p2p.wallet.jupiter.repository.tokens.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SwapTokensDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwapTokens(entities: List<SwapTokenEntity>)

    @Query(
        """
           SELECT * FROM swap_tokens 
           WHERE LOWER(symbol) LIKE :mintAddressOrSymbol 
           OR LOWER(address) LIKE :mintAddressOrSymbol
        """
    )
    suspend fun searchTokens(mintAddressOrSymbol: String): List<SwapTokenEntity>

    @Query("SELECT * FROM swap_tokens WHERE LOWER(address) = LOWER(:mintAddress)")
    suspend fun findTokenByMint(mintAddress: String): SwapTokenEntity?

    @Query("SELECT * FROM swap_tokens WHERE LOWER(symbol) = LOWER(:symbol)")
    suspend fun findTokenBySymbol(symbol: String): SwapTokenEntity?

    @Query("SELECT * FROM swap_tokens WHERE address NOT IN (:userTokensMints)")
    suspend fun findTokensExcludingMints(userTokensMints: Set<String>): List<SwapTokenEntity>

    @Query("SELECT * FROM swap_tokens WHERE address IN (:mints)")
    suspend fun findTokensByMints(mints: Set<String>): List<SwapTokenEntity>

    // todo: should be done with pagination
    @Query("SELECT * from swap_tokens")
    suspend fun getAllSwapTokens(): List<SwapTokenEntity>

    @Query("SELECT COUNT(*) from swap_tokens")
    suspend fun getAllSwapTokensSize(): Long
}
