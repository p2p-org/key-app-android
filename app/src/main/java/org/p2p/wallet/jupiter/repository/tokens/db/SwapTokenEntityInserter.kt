package org.p2p.wallet.jupiter.repository.tokens.db

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.p2p.wallet.jupiter.api.response.tokens.JupiterTokenResponse

class SwapTokenEntityInserter(
    private val dao: SwapTokensDao
) {
    suspend fun insertTokens(
        tokens: List<JupiterTokenResponse>
    ) {
        supervisorScope {
            val chunkSize = 600
            var chunkOffset = 0
            tokens.asSequence()
                .chunked(chunkSize)
                .forEach {
                    launch {
                        dao.insertSwapTokens(
                            it.mapIndexedNotNull { index, data ->
                                data.toEntity(index + chunkOffset)
                            }
                        )
                    }
                    chunkOffset += chunkSize
                }
        }
    }

    private fun JupiterTokenResponse.toEntity(ordinalIndex: Int?): SwapTokenEntity? {
        ordinalIndex ?: return null
        return SwapTokenEntity(
            ordinalIndex = ordinalIndex,
            address = address,
            chainId = chainId,
            decimals = decimals,
            logoUri = logoUri,
            name = name,
            symbol = symbol,
            coingeckoId = extensions?.coingeckoId
        )
    }
}
