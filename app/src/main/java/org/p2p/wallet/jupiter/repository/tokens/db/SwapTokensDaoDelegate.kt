package org.p2p.wallet.jupiter.repository.tokens.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.TokenExtensions
import org.p2p.wallet.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.user.repository.UserLocalRepository

class SwapTokensDaoDelegate(
    private val dao: SwapTokensDao,
    private val userLocalRepository: UserLocalRepository
) {
    private val tokenEntityInserter = SwapTokenEntityInserter(dao)

    suspend fun insertSwapTokens(
        tokens: List<JupiterTokenResponse>
    ): List<JupiterSwapToken> {
        tokenEntityInserter.insertTokens(tokens)

        return getAllTokens()
    }

    suspend fun getAllTokens(): List<JupiterSwapToken> {
        return dao.getAllSwapTokens().toDomain()
    }

    suspend fun getSwappableTokens(mintAddress: Base58String): List<JupiterSwapToken> {
        return dao.getSwappableTokens(mintAddress.base58Value).toDomain()
    }

    suspend fun searchTokens(
        mintAddressOrSymbol: String,
        swappableForMint: Base58String? = null
    ): List<JupiterSwapToken> {
        // % is needed for pattern matching
        return if (swappableForMint == null) {
            dao.searchTokens("${mintAddressOrSymbol.lowercase()}%")
        } else {
            dao.searchTokensInSwappable(
                mintAddressOrSymbol = "${mintAddressOrSymbol.lowercase()}%"
            )
        }
            .toDomain()
    }

    private suspend fun List<SwapTokenEntity>.toDomain(): List<JupiterSwapToken> {
        return mapNotNull { it.toDomain() }
            .asSequence()
            .filterTokensByAvailability()
            .filter { it.tokenSymbol.isNotBlank() }
            .toList()
    }

    private suspend fun SwapTokenEntity.toDomain(): JupiterSwapToken {
        val token = userLocalRepository.findTokenByMint(address)
        return JupiterSwapToken(
            tokenMint = token?.mintAddress?.toBase58Instance() ?: address.toBase58Instance(),
            chainId = chainId,
            decimals = token?.decimals ?: decimals,
            coingeckoId = coingeckoId,
            logoUri = token?.iconUrl ?: logoUri.orEmpty(),
            tokenName = token?.tokenName ?: name,
            tokenSymbol = token?.tokenSymbol ?: symbol,
            tokenExtensions = token?.tokenExtensions ?: TokenExtensions.NONE
        )
    }

    private fun Sequence<JupiterSwapToken>.filterTokensByAvailability(): Sequence<JupiterSwapToken> {
        return filter { it.tokenExtensions.isTokenCellVisibleOnWalletScreen != false }
    }

    suspend fun findTokenByMint(mintAddress: Base58String): JupiterSwapToken? = withContext(Dispatchers.IO) {
        dao.findTokenByMint(mintAddress.base58Value)?.toDomain()
    }

    suspend fun findTokenBySymbol(symbol: String): JupiterSwapToken? = withContext(Dispatchers.IO) {
        dao.findTokenBySymbol(symbol.trim().lowercase())?.toDomain()
    }

    suspend fun findTokensExcludingMints(
        excludingMints: Set<String>
    ): List<JupiterSwapToken> = withContext(Dispatchers.IO) {
        dao.findTokensExcludingMints(excludingMints).toDomain()
    }

    suspend fun findTokensIncludingMints(
        includingMints: Set<String>
    ): List<JupiterSwapToken> = withContext(Dispatchers.IO) {
        dao.findTokensIncludingMints(includingMints).toDomain()
    }
}
