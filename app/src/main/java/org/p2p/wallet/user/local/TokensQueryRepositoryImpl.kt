package org.p2p.wallet.user.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.receive.list.TokenListData
import org.p2p.wallet.token.model.TokenData

private const val DEFAULT_TOKEN_KEY = "DEFAULT_TOKEN_KEY"
private const val DEFAULT_QUERY_SIZE = 20

class TokensQueryRepositoryImpl : TokensQueryRepository {

    private val popularItems = arrayOf("SOL", "USDC", "BTC", "USDT", "ETH")
    private val tokensSearchResultFlow = MutableStateFlow(TokenListData())
    private val searchTextByTokens: MutableMap<String, List<TokenData>> = mutableMapOf()

    override fun getQueryTokensFlow(): Flow<TokenListData> = tokensSearchResultFlow

    override fun fetchTokens(tokens: List<TokenData>, searchText: String, refresh: Boolean) {
        if (refresh) {
            searchTextByTokens.clear()
            tokensSearchResultFlow.value = TokenListData()
        }
        val currentSearchTokenResultSize = tokensSearchResultFlow.value.size
        if (currentSearchTokenResultSize >= tokens.size) {
            return
        }

        when {
            searchText.isEmpty() -> {
                val defaultTokens = searchTextByTokens.getOrDefault(DEFAULT_TOKEN_KEY, emptyList())
                val newSize = (defaultTokens.size - getPopularDecimals(tokens).size) + DEFAULT_QUERY_SIZE
                val needToLoadMore = newSize > defaultTokens.size
                if (needToLoadMore) {
                    searchTextByTokens[DEFAULT_TOKEN_KEY] = getPopularDecimals(tokens) + tokens.take(newSize)
                }
                setResult(DEFAULT_TOKEN_KEY)
            }
            searchText in searchTextByTokens -> {
                val tokensBySearchText = searchTextByTokens.getOrDefault(searchText, emptyList())
                val newSize = tokensBySearchText.size + DEFAULT_QUERY_SIZE
                val needToLoadMore = newSize > tokensBySearchText.size
                if (needToLoadMore) {
                    loadMoreTokensBySearchText(tokens, searchText, newSize)
                }
                setResult(searchText)
            }
            searchText !in searchTextByTokens -> {
                loadMoreTokensBySearchText(tokens, searchText, DEFAULT_QUERY_SIZE)
                setResult(searchText)
            }
        }
        TODO("Not yet implemented")
    }

    private fun setResult(key: String) {
        val result = TokenListData(
            searchText = key,
            result = searchTextByTokens[key].orEmpty()
        )
        tokensSearchResultFlow.value = result
    }

    private fun loadMoreTokensBySearchText(tokens: List<TokenData>, searchText: String, newSearchTokensSize: Int) {
        val searchResult = findTokensBySearchText(tokens, searchText)
        searchTextByTokens[searchText] = searchResult.take(newSearchTokensSize)
    }

    private fun findTokensBySearchText(tokens: List<TokenData>, searchText: String): List<TokenData> {
        return tokens
            .asSequence()
            .filter { token -> searchText == token.symbol || token.name.startsWith(searchText, ignoreCase = true) }
            .toList()
    }

    private fun getPopularDecimals(tokens: List<TokenData>): List<TokenData> {
        val items = mutableListOf<TokenData>()
        for (symbol in popularItems) {
            val token = tokens.firstOrNull { it.symbol == symbol }
            if (token != null) items.add(token)
        }
        return items
    }
}
