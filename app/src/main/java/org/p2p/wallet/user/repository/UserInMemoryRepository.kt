package org.p2p.wallet.user.repository

import timber.log.Timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.receive.list.TokenListData

private const val DEFAULT_TOKEN_KEY = "DEFAULT_TOKEN_KEY"

private const val TAG = "UserInMemoryRepository"

class UserInMemoryRepository(
    private val tokenConverter: TokenConverter,
    private val tokenServiceRepository: TokenServiceRepository
) : UserLocalRepository {
    private val popularItems = arrayOf("SOL", "USDC", "BTC", "USDT", "ETH")
    private val allTokensFlow = MutableStateFlow<List<TokenData>>(emptyList())

    private val tokensSearchResultFlow = MutableStateFlow(TokenListData())
    private val searchTextByTokens: MutableMap<String, List<TokenData>> = mutableMapOf()

    override fun areInitialTokensLoaded(): Boolean {
        return allTokensFlow.value.isNotEmpty()
    }

    override fun setTokenData(data: List<TokenData>) {
        allTokensFlow.value = data.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    override fun getTokensData(): List<TokenData> = allTokensFlow.value

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
        val filteredResult = searchTextByTokens[key].orEmpty().filter { it.symbol != Constants.WSOL_SYMBOL }
        val searchResult = TokenListData(
            searchText = key,
            result = filteredResult
        )
        tokensSearchResultFlow.value = searchResult
    }

    private fun loadMoreTokensBySearchText(searchText: String, newSearchTokensSize: Int) {
        val searchResult = findTokensBySearchText(searchText)
        searchTextByTokens[searchText] = searchResult.take(newSearchTokensSize)
    }

    private fun findTokensBySearchText(searchText: String): List<TokenData> {
        val filteredList = mutableListOf<TokenData>()

        val allTokens = allTokensFlow.value

        // Filter items that start with the query
        allTokens.filterTo(filteredList) {
            val isNamesEqualToSearchText = it.name.equals(searchText, ignoreCase = true)
            val isSymbolEqualsToSearchText = it.symbol.equals(searchText, ignoreCase = true)
            val isNameStartsWithSearchText = it.name.startsWith(searchText, ignoreCase = true)
            val isSymbolStartsWithSearchText = it.symbol.startsWith(searchText, ignoreCase = true)
            isNamesEqualToSearchText || isSymbolEqualsToSearchText ||
                isNameStartsWithSearchText || isSymbolStartsWithSearchText
        }

        return filteredList.distinctBy { it.symbol }.sortedByDescending {
            it.symbol.equals(searchText, ignoreCase = true)
        }
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

    override suspend fun findTokenByMint(mintAddress: String): Token? {
        val tokenData: TokenData? = findTokenData(mintAddress)
        return if (tokenData != null) {
            val price = tokenServiceRepository.findTokenPriceByAddress(
                tokenAddress = tokenData.mintAddress,
            )
            tokenConverter.fromNetwork(tokenData, price)
        } else {
            null
        }
    }
}
