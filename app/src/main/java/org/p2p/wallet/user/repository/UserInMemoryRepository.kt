package org.p2p.wallet.user.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.receive.list.TokenListData
import org.p2p.wallet.user.model.TokenData
import timber.log.Timber

private const val DEFAULT_TOKEN_KEY = "DEFAULT_TOKEN_KEY"

class UserInMemoryRepository : UserLocalRepository {
    private val popularItems = arrayOf("SOL", "USDC", "BTC", "USDT", "ETH")
    private val pricesFlow = MutableStateFlow<List<TokenPrice>>(emptyList())
    private val tokensFlow = MutableStateFlow<List<TokenData>>(emptyList())

    private val tokensSearchResultFlow = MutableStateFlow<TokenListData>(TokenListData())
    private val searchTextByTokens: MutableMap<String, List<TokenData>> = mutableMapOf()

    override fun setTokenPrices(prices: List<TokenPrice>) {
        pricesFlow.value = prices
    }

    override fun getPriceByToken(symbol: String): TokenPrice? =
        pricesFlow.value.firstOrNull { it.tokenSymbol == symbol }

    private val allTokensFlow = MutableStateFlow<List<TokenData>>(emptyList())

    override fun setTokenData(data: List<TokenData>) {
        allTokensFlow.value = data.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    override fun fetchTokens(searchText: String, count: Int, refresh: Boolean) {
        if (refresh) {
            searchTextByTokens.clear()
            tokensSearchResultFlow.value = TokenListData()
        }
        val allInMemoryTokens = allTokensFlow.value
        val currentSearchTokenResultSize = tokensSearchResultFlow.value.size
        if (currentSearchTokenResultSize >= allInMemoryTokens.size) {
            return
        }

        when {
            searchText.isEmpty() -> {
                val defaultTokens = searchTextByTokens.getOrDefault(DEFAULT_TOKEN_KEY, emptyList())
                val newSize = (defaultTokens.size - getPopularDecimals().size) + count
                val needToLoadMore = newSize > defaultTokens.size
                if (needToLoadMore) {
                    searchTextByTokens[DEFAULT_TOKEN_KEY] = getPopularDecimals() + allInMemoryTokens.take(newSize)
                }
                setResult(DEFAULT_TOKEN_KEY)
            }
            searchText in searchTextByTokens -> {
                val tokensBySearchText = searchTextByTokens.getOrDefault(searchText, emptyList())
                val newSize = tokensBySearchText.size + count
                val needToLoadMore = newSize > tokensBySearchText.size
                if (needToLoadMore) {
                    loadMoreTokensBySearchText(searchText, newSize)
                }
                setResult(searchText)
            }
            searchText !in searchTextByTokens -> {
                loadMoreTokensBySearchText(searchText, count)
                setResult(searchText)
            }
        }
    }

    private fun setResult(key: String) {
        val result = TokenListData(
            searchText = key,
            result = searchTextByTokens[key].orEmpty()
        )
        tokensSearchResultFlow.value = result
    }
    private fun loadMoreTokensBySearchText(searchText: String, newSearchTokensSize: Int) {
        val searchResult = findTokensBySearchText(searchText)
        searchTextByTokens[searchText] = searchResult.take(newSearchTokensSize)
    }

    private fun findTokensBySearchText(searchText: String): List<TokenData> {
        return allTokensFlow.value
            .asSequence()
            .filter { token -> searchText == token.symbol || token.name.startsWith(searchText, ignoreCase = true) }
            .toList()
    }

    override fun getTokenListFlow(): Flow<TokenListData> = tokensSearchResultFlow

    override fun findTokenData(mintAddress: String): TokenData? {
        val data = allTokensFlow.value.firstOrNull { it.mintAddress == mintAddress }
        if (data == null) {
            Timber.w("No data found for $mintAddress")
        }

        return data
    }

    override fun findTokenDataBySymbol(symbol: String): TokenData? {
        val data = allTokensFlow.value.firstOrNull { it.symbol == symbol }
        if (data == null) {
            Timber.w("No data found for $symbol")
        }

        return data
    }

    private fun getPopularDecimals(): List<TokenData> {
        val items = mutableListOf<TokenData>()
        val tokens = allTokensFlow.value
        for (symbol in popularItems) {
            val token = tokens.firstOrNull { it.symbol == symbol }
            if (token != null) items.add(token)
        }
        return items
    }
}
