package org.p2p.wallet.jupiter.repository.tokens.db

import com.google.gson.stream.JsonReader
import okhttp3.ResponseBody
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
    private val routeEntityInserter = SwapRouteEntityInserter(dao)

    suspend fun insertSwapTokens(
        routesResponse: ResponseBody,
        tokens: List<JupiterTokenResponse>
    ): List<JupiterSwapToken> {
        val jsonReader = JsonReader(routesResponse.charStream())
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "mintKeys" -> tokenEntityInserter.insertTokens(jsonReader, tokens)
                "indexedRouteMap" -> routeEntityInserter.insertRoutes(jsonReader)
            }
        }
        jsonReader.endObject()

        jsonReader.close()
        routesResponse.close()

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
                mintAddress = swappableForMint.base58Value,
                mintAddressOrSymbol = "${mintAddressOrSymbol.lowercase()}%"
            )
        }
            .toDomain()
    }

    private suspend fun List<SwapTokenEntity>.toDomain(): List<JupiterSwapToken> {
        return map { entity ->
            val token = userLocalRepository.findTokenByMint(entity.address)
            JupiterSwapToken(
                tokenMint = token?.mintAddress?.toBase58Instance() ?: entity.address.toBase58Instance(),
                chainId = entity.chainId,
                decimals = token?.decimals ?: entity.decimals,
                coingeckoId = entity.coingeckoId,
                logoUri = token?.iconUrl ?: entity.logoUri.orEmpty(),
                tokenName = token?.tokenName ?: entity.name,
                tokenSymbol = token?.tokenSymbol ?: entity.symbol,
                tokenExtensions = token?.tokenExtensions ?: TokenExtensions.NONE
            )
        }
            .asSequence()
            .filterTokensByAvailability()
            .filter { it.tokenSymbol.isNotBlank() }
            .toList()
    }

    private fun Sequence<JupiterSwapToken>.filterTokensByAvailability(): Sequence<JupiterSwapToken> {
        return filter { it.tokenExtensions.isTokenCellVisibleOnWalletScreen != false }
    }
}
