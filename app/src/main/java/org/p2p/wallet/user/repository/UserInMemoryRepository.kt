package org.p2p.wallet.user.repository

import timber.log.Timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.receive.list.TokenListData

private const val DEFAULT_TOKEN_KEY = "DEFAULT_TOKEN_KEY"

private const val TAG = "UserInMemoryRepository"

class UserInMemoryRepository(
    private val tokenConverter: TokenConverter
) : UserLocalRepository {
    private val popularItems = arrayOf("SOL", "USDC", "BTC", "USDT", "ETH")
    private val pricesFlow = MutableStateFlow<List<TokenPrice>>(emptyList())
    private val allTokensFlow = MutableStateFlow<List<TokenData>>(emptyList())

    private val tokensSearchResultFlow = MutableStateFlow(TokenListData())
    private val searchTextByTokens: MutableMap<String, List<TokenData>> = mutableMapOf()

    override fun setTokenPrices(prices: List<TokenPrice>) {
        pricesFlow.value = prices
    }

    override fun getTokenPrices(): Flow<List<TokenPrice>> = pricesFlow

    override fun getPriceByTokenId(tokenId: String?): TokenPrice? {
        return pricesFlow.value.firstOrNull { it.tokenId == tokenId }
    }

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
                setSearchResult(DEFAULT_TOKEN_KEY)
            }
            searchText in searchTextByTokens -> {
                val tokensBySearchText = searchTextByTokens.getOrDefault(searchText, emptyList())
                val newSize = tokensBySearchText.size + count
                val needToLoadMore = newSize > tokensBySearchText.size
                if (needToLoadMore) {
                    loadMoreTokensBySearchText(searchText, newSize)
                }
                setSearchResult(searchText)
            }
            searchText !in searchTextByTokens -> {
                loadMoreTokensBySearchText(searchText, count)
                setSearchResult(searchText)
            }
        }
    }

    private fun setSearchResult(key: String) {
        val searchResult = TokenListData(
            searchText = key,
            result = searchTextByTokens[key].orEmpty()
        )
        tokensSearchResultFlow.value = searchResult
    }

    private fun loadMoreTokensBySearchText(searchText: String, newSearchTokensSize: Int) {
        val searchResult = findTokensBySearchText(searchText)
        searchTextByTokens[searchText] = searchResult.take(newSearchTokensSize)
    }

    private fun findTokensBySearchText(searchText: String): List<TokenData> {
        return allTokensFlow.value
            .asSequence()
            .filter { token ->
                token.symbol.contains(searchText, ignoreCase = true) ||
                    token.name.startsWith(searchText, ignoreCase = true)
            }
            .toList()
    }

    override fun getTokenListFlow(): Flow<TokenListData> = tokensSearchResultFlow

    override fun findTokenData(mintAddress: String): TokenData? {
        val resultToken = allTokensFlow.value.firstOrNull { it.mintAddress == mintAddress }
        if (resultToken == null) {
            Timber.tag(TAG).i("No user token found for mint $mintAddress")
        }

        return resultToken
    }

    override fun findTokenDataBySymbol(symbol: String): TokenData? {
        val resultToken = allTokensFlow.value.firstOrNull { it.symbol == symbol }
        if (resultToken == null) {
            Timber.tag(TAG).i("No token found for symbol $symbol")
        }

        return resultToken
    }

    private fun getPopularDecimals(): List<TokenData> {
        val popularTokens = mutableListOf<TokenData>()
        val tokens = allTokensFlow.value
        for (symbol in popularItems) {
            val token = tokens.firstOrNull { it.symbol == symbol }
            if (token != null) popularTokens.add(token)
        }
        return popularTokens
    }

    override fun findTokenByMint(mintAddress: String): Token? {
        val tokenData: TokenData? = findTokenData(mintAddress)
        return if (tokenData != null) {
            val price = getPriceByTokenId(tokenData.coingeckoId)
            tokenConverter.fromNetwork(tokenData, price)
        } else {
            null
        }
    }
}
