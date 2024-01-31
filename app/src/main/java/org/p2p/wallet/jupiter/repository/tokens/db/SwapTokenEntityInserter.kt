package org.p2p.wallet.jupiter.repository.tokens.db

import org.json.JSONArray
import timber.log.Timber
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.p2p.wallet.jupiter.api.response.tokens.JupiterTokenResponse

class SwapTokenEntityInserter(
    private val dao: SwapTokensDao
) {
    suspend fun insertTokens(tokens: List<JupiterTokenResponse>) {
        supervisorScope {
            val chunkSize = 600
            var chunkOffset = 0
            Timber.i("Inserting tokens total: ${tokens.size}")
            tokens.asSequence()
                .chunked(chunkSize)
                .forEach {
                    Timber.i("Inserting tokens: ${it.size}")
                    launch {
                        dao.insertSwapTokens(it.map(::toEntity))
                    }
                    chunkOffset += chunkSize
                }
        }
    }

    private fun toEntity(response: JupiterTokenResponse): SwapTokenEntity {
        return SwapTokenEntity(
            address = response.address,
            chainId = response.chainId,
            decimals = response.decimals,
            logoUri = response.logoUri,
            name = response.name,
            symbol = response.symbol,
            tagsAsJsonList = JSONArray(response.tags).toString(),
            coingeckoId = response.extensions?.coingeckoId
        )
    }
}
