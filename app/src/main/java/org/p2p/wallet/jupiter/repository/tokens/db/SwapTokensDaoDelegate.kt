package org.p2p.wallet.jupiter.repository.tokens.db

import org.json.JSONArray
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.TokenExtensions
import org.p2p.wallet.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toStringSet

class SwapTokensDaoDelegate(
    private val dao: SwapTokensDao,
    private val userLocalRepository: UserLocalRepository
) {
    private val tokenEntityInserter = SwapTokenEntityInserter(dao)

    suspend fun insertSwapTokens(tokens: List<JupiterTokenResponse>): List<JupiterSwapToken> {
        tokenEntityInserter.insertTokens(tokens)

        return getAllTokens()
    }

    suspend fun getAllTokens(): List<JupiterSwapToken> {
        return dao.getAllSwapTokens().toDomain()
    }

    suspend fun searchTokens(
        mintAddressOrSymbol: String,
    ): List<JupiterSwapToken> {
        // % is needed for pattern matching
        return dao.searchTokens("$mintAddressOrSymbol%")
            .toDomain()
    }

    private suspend fun List<SwapTokenEntity>.toDomain(): List<JupiterSwapToken> {
        return map { it.toDomain() }
            .asSequence()
            .filterTokensByAvailability()
            .filter { it.tokenSymbol.isNotBlank() }
            .toList()
    }

    private suspend fun SwapTokenEntity.toDomain(): JupiterSwapToken {
        val appTokens = userLocalRepository.findTokenByMint(address)
        return JupiterSwapToken(
            tokenMint = appTokens?.mintAddress?.toBase58Instance() ?: address.toBase58Instance(),
            chainId = chainId,
            decimals = appTokens?.decimals ?: decimals,
            coingeckoId = coingeckoId,
            logoUri = appTokens?.iconUrl ?: logoUri.orEmpty(),
            tokenName = appTokens?.tokenName ?: name,
            tokenSymbol = appTokens?.tokenSymbol ?: symbol,
            tags = JSONArray(tagsAsJsonList).toStringSet(),
            tokenExtensions = appTokens?.tokenExtensions ?: TokenExtensions.NONE,
        )
    }

    private fun Sequence<JupiterSwapToken>.filterTokensByAvailability(): Sequence<JupiterSwapToken> {
        return filter { it.tokenExtensions.isTokenCellVisibleOnWalletScreen != false }
    }

    suspend fun findTokenByMint(mintAddress: Base58String): JupiterSwapToken? =
        dao.findTokenByMint(mintAddress.base58Value)?.toDomain()

    suspend fun findTokenBySymbol(symbol: String): JupiterSwapToken? =
        dao.findTokenBySymbol(symbol.trim().lowercase())?.toDomain()

    suspend fun findTokensExcludingMints(excludingMints: Set<String>): List<JupiterSwapToken> =
        dao.findTokensExcludingMints(excludingMints).toDomain()

    suspend fun findTokensByMints(mints: Set<String>): List<JupiterSwapToken> =
        dao.findTokensByMints(mints).toDomain()
}
