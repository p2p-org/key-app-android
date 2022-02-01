package org.p2p.wallet.user.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.main.model.TokenPrice
import org.p2p.wallet.user.model.TokenData
import timber.log.Timber

class UserInMemoryRepository : UserLocalRepository {
    private val popularItems = arrayOf("SOL", "USDC", "BTC", "USDT", "ETH")
    private val pricesFlow = MutableStateFlow<List<TokenPrice>>(emptyList())
    private val tokensFlow = MutableStateFlow<List<TokenData>>(emptyList())

    override fun setTokenPrices(prices: List<TokenPrice>) {
        pricesFlow.value = prices
    }

    override fun getPriceByToken(symbol: String): TokenPrice? =
        pricesFlow.value.firstOrNull { it.tokenSymbol == symbol }

    private val decimalsFlow = MutableStateFlow<List<TokenData>>(emptyList())

    override fun setTokenData(data: List<TokenData>) {
        decimalsFlow.value = data
    }

    override fun fetchTokens(searchText: String, count: Int, refresh: Boolean) {
        if (refresh) {
            tokensFlow.value = emptyList()
        }
        if (tokensFlow.value.size >= decimalsFlow.value.size) {
            return
        }
        val items = decimalsFlow.value
        val offset = tokensFlow.value.size

        if (searchText.isNotEmpty()) {
            val result = items.filter {
                searchText == it.symbol ||
                    it.name.startsWith(searchText, ignoreCase = true)
            }
            val indexes = findEdges(items = result, count = count, offset = offset)
            appendTokens(result.subList(indexes.first, indexes.second))
        }

        val indexes = findEdges(items = items, count = count, offset = offset)
        val result = items.subList(indexes.first, indexes.second)

        if (offset == 0 && searchText.isEmpty()) {
            appendTokens(getPopularDecimals() + result)
            return
        }
        appendTokens(result)
    }

    override fun getTokenListFlow(): Flow<List<TokenData>> = tokensFlow

    override fun findTokenData(mintAddress: String): TokenData? {
        val data = decimalsFlow.value.firstOrNull { it.mintAddress == mintAddress }
        if (data == null) {
            Timber.w("No data found for $mintAddress")
        }

        return data
    }

    override fun findTokenDataBySymbol(symbol: String): TokenData? {
        val data = decimalsFlow.value.firstOrNull { it.symbol == symbol }
        if (data == null) {
            Timber.w("No data found for $symbol")
        }

        return data
    }

    private fun getPopularDecimals(): List<TokenData> {
        val items = mutableListOf<TokenData>()
        val tokens = decimalsFlow.value
        for (symbol in popularItems) {
            val token = tokens.firstOrNull { it.symbol == symbol }
            if (token != null) items.add(token)
        }
        return items
    }

    private fun appendTokens(items: List<TokenData>) {
        tokensFlow.value = tokensFlow.value + items
    }

    private fun <T> findEdges(items: List<T>, offset: Int, count: Int): Pair<Int, Int> {
        val endIndex = if (offset + count > items.size) items.size else offset + count
        val startIndex = if (offset > items.size) 0 else offset
        return Pair(startIndex, endIndex)
    }
}