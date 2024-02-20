package org.p2p.wallet.user.repository

import timber.log.Timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.core.token.TokenMetadata
import org.p2p.core.utils.Constants
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.receive.list.TokenListData

private const val DEFAULT_TOKEN_KEY = "DEFAULT_TOKEN_KEY"

private const val TAG = "UserInMemoryRepository"

// TODO: https://p2pvalidator.atlassian.net/browse/PWN-9649
class UserInMemoryRepository(
    private val tokenConverter: TokenConverter,
    private val tokenServiceRepository: TokenServiceRepository
) : UserLocalRepository {
    private val popularTokensMints = arrayOf(
        Constants.WRAPPED_SOL_MINT,
        Constants.USDC_MINT,
        Constants.WRAPPED_BTC_MINT,
        Constants.USDT_MINT,
        Constants.WRAPPED_ETH_MINT,
    )
    private val allTokensFlow = MutableStateFlow<List<TokenMetadata>>(emptyList())

    private val tokensSearchResultFlow = MutableStateFlow(TokenListData())
    private val searchTextByTokens: MutableMap<String, List<TokenMetadata>> = mutableMapOf()

    override fun areInitialTokensLoaded(): Boolean {
        return allTokensFlow.value.isNotEmpty()
    }

    override fun setTokenData(data: List<TokenMetadata>) {
        allTokensFlow.value = data.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    override fun getTokensData(): List<TokenMetadata> = allTokensFlow.value

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

        val popularTokens = getPopularTokensMetadata()

        when {
            searchText.isEmpty() -> {
                val defaultTokens = searchTextByTokens.getOrDefault(DEFAULT_TOKEN_KEY, emptyList())
                val newSize = (defaultTokens.size - popularTokens.size) + count
                val needToLoadMore = newSize > defaultTokens.size
                if (needToLoadMore) {
                    searchTextByTokens[DEFAULT_TOKEN_KEY] = popularTokens + allInMemoryTokens.take(newSize)
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

    private fun findTokensBySearchText(searchText: String): List<TokenMetadata> {
        val filteredList = mutableListOf<TokenMetadata>()

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

    override fun findTokenData(mintAddress: String): TokenMetadata? {
        return allTokensFlow.value.firstOrNull { it.mintAddress == mintAddress }
    }

    override fun findTokenDataBySymbol(symbol: String): TokenMetadata? {
        val resultToken = allTokensFlow.value.firstOrNull { it.symbol == symbol }
        if (resultToken == null) {
            Timber.tag(TAG).i("No token found for symbol $symbol")
        }

        return resultToken
    }

    private fun getPopularTokensMetadata(): List<TokenMetadata> {
        val allTokens = allTokensFlow.value
        return popularTokensMints.mapNotNull { popularMint ->
            allTokens.find { it.mintAddress == popularMint }
        }
    }

    @Deprecated("This repository should not return [Token] objects but only [TokenMetadata]")
    override suspend fun findTokenByMint(mintAddress: String): Token? {
        val tokenMetadata: TokenMetadata? = findTokenData(mintAddress)
        return if (tokenMetadata != null) {
            val price = tokenServiceRepository.getTokenPriceByAddress(
                tokenAddress = tokenMetadata.mintAddress,
                networkChain = TokenServiceNetwork.SOLANA
            )
            tokenConverter.fromNetwork(tokenMetadata, price)
        } else {
            null
        }
    }
}
