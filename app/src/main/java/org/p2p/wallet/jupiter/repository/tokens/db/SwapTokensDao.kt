package org.p2p.wallet.jupiter.repository.tokens.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.intellij.lang.annotations.Language

@Dao
interface SwapTokensDao {
    private companion object {
        @Language("RoomSql")
        private const val SWAPPABLE_TOKEN_INDEXES_FOR_TOKEN =
            "SELECT swap_tokens_routes.index_swappable_token " +
                "FROM swap_tokens_routes " +
                "JOIN swap_tokens ON swap_tokens_routes.index_source_token = swap_tokens.ordinal_index " +
                "WHERE swap_tokens.address = :mintAddress "
    }

    @Insert
    suspend fun insertSwapTokens(entities: List<SwapTokenEntity>)

    @Insert
    suspend fun insertTokenRoutes(routes: List<SwapTokenRouteCrossRef>)

    @Query("SELECT * FROM swap_tokens WHERE ordinal_index IN ($SWAPPABLE_TOKEN_INDEXES_FOR_TOKEN)")
    suspend fun getSwappableTokens(mintAddress: String): List<SwapTokenEntity>

    @Query(
        "SELECT * FROM swap_tokens " +
            "WHERE ordinal_index IN ($SWAPPABLE_TOKEN_INDEXES_FOR_TOKEN) " +
            "AND (LOWER(symbol) LIKE :mintAddressOrSymbol OR LOWER(address) LIKE :mintAddressOrSymbol)"
    )
    suspend fun searchTokensInSwappable(mintAddress: String, mintAddressOrSymbol: String): List<SwapTokenEntity>

    @Query(
        "SELECT * FROM swap_tokens " +
            "WHERE LOWER(symbol) LIKE :mintAddressOrSymbol " +
            "OR LOWER(address) LIKE :mintAddressOrSymbol"
    )
    suspend fun searchTokens(mintAddressOrSymbol: String): List<SwapTokenEntity>

    @Query("SELECT * from swap_tokens where address = :mintAddress")
    suspend fun getSwapToken(mintAddress: String): SwapTokenEntity?

    @Query("SELECT * from swap_tokens ")
    suspend fun getAllSwapTokens(): List<SwapTokenEntity>
}
