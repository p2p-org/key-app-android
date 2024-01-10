package org.p2p.wallet.jupiter.repository.tokens.db

import androidx.collection.SimpleArrayMap
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.p2p.wallet.jupiter.api.response.tokens.JupiterTokenResponse

class SwapTokenEntityInserter(
    private val dao: SwapTokensDao
) {

    private fun createAddressToIndexArray(routesArray: JsonReader): SimpleArrayMap<String, Int> {
        routesArray.beginArray()
        var index = 0
        val addressToIndex = SimpleArrayMap<String, Int>(2000)
        while (routesArray.hasNext()) {
            val mintAddress = routesArray.nextString()
            addressToIndex.put(mintAddress, index)
            index += 1
        }
        routesArray.endArray()
        return addressToIndex
    }

    suspend fun insertTokens(
        mintsArray: JsonReader,
        tokens: List<JupiterTokenResponse>
    ) {
        supervisorScope {
            val addressToIndex = createAddressToIndexArray(mintsArray)
            tokens.asSequence()
                .mapNotNull { it.toEntity(addressToIndex[it.address]) }
                .chunked(600)
                .forEach {
                    launch {
                        dao.insertSwapTokens(it)
                    }
                }

            addressToIndex.clear()
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
