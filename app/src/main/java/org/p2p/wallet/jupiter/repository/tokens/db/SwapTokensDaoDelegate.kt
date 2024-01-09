package org.p2p.wallet.jupiter.repository.tokens.db

import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.TokenExtensions
import org.p2p.wallet.jupiter.api.response.JupiterAllSwapRoutesResponse
import org.p2p.wallet.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.user.repository.UserLocalRepository

class SwapTokensDaoDelegate(
    private val dao: SwapTokensDao,
    private val userLocalRepository: UserLocalRepository
) {
    suspend fun insertSwapTokens(
        routes: JupiterAllSwapRoutesResponse,
        tokens: List<JupiterTokenResponse>
    ): List<JupiterSwapToken> = supervisorScope {
        val savedTokens = async {
            val addressToIndex = routes.mintKeys
                .mapIndexed { index, address -> address.base58Value to index }
                .toMap()

            val entities = tokens.mapNotNull {
                SwapTokenEntity(
                    ordinalIndex = addressToIndex[it.address] ?: return@mapNotNull null,
                    address = it.address,
                    chainId = it.chainId,
                    decimals = it.decimals,
                    logoUri = it.logoUri,
                    name = it.name,
                    symbol = it.symbol,
                    coingeckoId = it.extensions?.coingeckoId
                )
            }
            dao.insertSwapTokens(entities)
            entities.toDomain()
        }
        launch {
            routes.routeMap
                .asSequence()
                .forEach { (indexA, indexesB) ->
                    val indexAToIndexBs = indexesB.map {
                        SwapTokenRouteCrossRef(indexA.toInt(), it)
                    }
                    dao.insertTokenRoutes(indexAToIndexBs)
                }
        }

        savedTokens.await()
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
            dao.searchTokens("$mintAddressOrSymbol%")
        } else {
            dao.searchTokensInSwappable(
                mintAddress = swappableForMint.base58Value,
                mintAddressOrSymbol = "$mintAddressOrSymbol%"
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
