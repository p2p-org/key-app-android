package org.p2p.wallet.user.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import timber.log.Timber
import java.util.Date
import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.common.feature_toggles.toggles.remote.TokenMetadataUpdateFeatureToggle
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.home.ui.main.POPULAR_TOKENS_COINGECKO_IDS
import org.p2p.wallet.home.ui.main.TOKEN_SYMBOLS_VALID_FOR_BUY
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.repository.RecipientsLocalRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRepository
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.utils.emptyString

private const val KEY_HIDDEN_TOKENS_VISIBILITY = "KEY_HIDDEN_TOKENS_VISIBILITY"

private const val TAG = "UserInteractor"
const val TOKENS_FILE_NAME = "tokens.json"

class UserInteractor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: HomeLocalRepository,
    private val recipientsLocalRepository: RecipientsLocalRepository,
    private val rpcRepository: RpcBalanceRepository,
    private val sharedPreferences: SharedPreferences,
    private val externalStorageRepository: ExternalStorageRepository,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val metadataUpdateFeatureToggle: TokenMetadataUpdateFeatureToggle,
    private val gson: Gson
) {

    fun findTokenData(mintAddress: String): Token? {
        val tokenData = userLocalRepository.findTokenData(mintAddress)
        val price = tokenData?.let { userLocalRepository.getPriceByTokenId(it.coingeckoId) }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }

    fun getUserTokensFlow(): Flow<List<Token.Active>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getSingleTokenForBuy(availableTokensSymbols: List<String> = TOKEN_SYMBOLS_VALID_FOR_BUY): Token? =
        getTokensForBuy(availableTokensSymbols).firstOrNull()

    suspend fun getTokensForBuy(
        availableTokensSymbols: List<String> = TOKEN_SYMBOLS_VALID_FOR_BUY
    ): List<Token> {
        val userTokens = getUserTokens()
        val allTokens = availableTokensSymbols.mapNotNull { tokenSymbol ->
            val userToken = userTokens.find { it.tokenSymbol == tokenSymbol }
            userToken ?: findTokenDataBySymbol(tokenSymbol)
        }

        if (allTokens.isEmpty()) {
            Timber.e(
                IllegalStateException("No tokens to buy! All tokens: ${userTokens.map(Token.Active::tokenSymbol)}")
            )
        }

        return allTokens
    }

    suspend fun getBalance(address: PublicKey): Long = rpcRepository.getBalance(address)

    suspend fun loadAllTokensDataIfEmpty() {
        if(!userLocalRepository.areInitialTokensLoaded()) {
            loadAllTokensData()
        }
    }

    suspend fun loadAllTokensData() {
        val file = externalStorageRepository.readJsonFile(TOKENS_FILE_NAME)

        if (!metadataUpdateFeatureToggle.isFeatureEnabled && file != null) {
            Timber.tag(TAG).i("Tokens data file was found. Trying to parse it...")
            val tokens = gson.fromJson(file.data, Array<TokenData>::class.java)?.toList()
            if (tokens != null) {
                Timber.tag(TAG).i("Tokens data were successfully parsed from file.")
                userLocalRepository.setTokenData(tokens)
                return
            }
        }

        Timber.tag(TAG).i("Tokens data file was not found. Loading from remote")
        // If the file is not found or empty, load from network
        val data = userRepository.loadAllTokens()
        userLocalRepository.setTokenData(data)

        // Save tokens to the file
        externalStorageRepository.saveJson(json = gson.toJson(data), fileName = TOKENS_FILE_NAME)
        return
    }

    fun fetchTokens(searchText: String = emptyString(), count: Int, refresh: Boolean) {
        userLocalRepository.fetchTokens(searchText, count, refresh)
    }

    fun getTokenListFlow() = userLocalRepository.getTokenListFlow()

    fun getHiddenTokensVisibility(): Boolean {
        return sharedPreferences.getBoolean(KEY_HIDDEN_TOKENS_VISIBILITY, false)
    }

    fun setHiddenTokensVisibility(visible: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_HIDDEN_TOKENS_VISIBILITY, visible)
        }
    }

    suspend fun loadUserRates(userTokens: List<Token.Active>) {
        Timber.i("Loading user rates for ${userTokens.size}")
        val tokenIds = userTokens.mapNotNull { token ->
            val coingeckoId = userLocalRepository.findTokenData(token.mintAddress)?.coingeckoId
            coingeckoId?.let { TokenId(id = it) }
        }

        val allTokenIds = (tokenIds + POPULAR_TOKENS_COINGECKO_IDS).distinct()

        val prices = tokenPricesRepository.getTokenPriceByIds(
            tokenIds = allTokenIds,
            targetCurrency = Constants.USD_READABLE_SYMBOL
        )

        userLocalRepository.setTokenPrices(prices)

        updateUserTokenRates(prices)
    }

    suspend fun loadUserTokensAndUpdateLocal(publicKey: PublicKey): List<Token.Active> {
        val newTokens = userRepository.loadUserTokens(publicKey)
        val cachedTokens = mainLocalRepository.getUserTokens()
        return updateLocalTokens(cachedTokens, newTokens)
    }

    private suspend fun updateUserTokenRates(prices: List<TokenPrice>) {
        val cachedTokens = mainLocalRepository.getUserTokens()

        val newTokens = cachedTokens.map { token ->
            val price = prices.find { it.tokenId == token.coingeckoId }
            val totalInUsd = price?.price?.let { token.total.times(it) }
            token.copy(rate = price?.price, totalInUsd = totalInUsd)
        }

        mainLocalRepository.clear()
        mainLocalRepository.updateTokens(newTokens)
    }

    private suspend fun updateLocalTokens(
        cachedTokens: List<Token.Active>,
        newTokens: List<Token.Active>
    ): List<Token.Active> {
        Timber.i("Updating local tokens: old=${cachedTokens.size};new=${newTokens.size}")
        val newTokensToCache = newTokens
            .map { newToken ->
                val oldToken = cachedTokens.find { oldToken -> oldToken.publicKey == newToken.publicKey }
                newToken.copy(visibility = oldToken?.visibility ?: newToken.visibility)
            }
            .sortedWith(TokenComparator())
        mainLocalRepository.clear()
        mainLocalRepository.updateTokens(newTokensToCache)
        return newTokensToCache
    }

    suspend fun getUserTokens(): List<Token.Active> =
        mainLocalRepository.getUserTokens()

    suspend fun getNonZeroUserTokens(): List<Token.Active> =
        mainLocalRepository.getUserTokens()
            .filterNot { it.isZero }

    suspend fun getUserSolToken(): Token.Active? =
        mainLocalRepository.getUserTokens().find { it.isSOL }

    suspend fun findUserToken(mintAddress: String): Token.Active? =
        mainLocalRepository.getUserTokens().find { it.mintAddress == mintAddress }

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        mainLocalRepository.setTokenHidden(mintAddress, visibility)

    suspend fun hasAccount(address: String): Boolean {
        val userTokens = mainLocalRepository.getUserTokens()
        return userTokens.any { it.publicKey == address }
    }

    fun findMultipleTokenData(tokenSymbols: List<String>): List<Token> =
        tokenSymbols.mapNotNull { findTokenDataBySymbol(it) }

    fun findMultipleTokenDataByAddresses(mintAddresses: List<String>): List<Token> =
        mintAddresses.mapNotNull { findTokenDataByAddress(it) }

    private fun findTokenDataBySymbol(symbol: String): Token? {
        val tokenData = userLocalRepository.findTokenDataBySymbol(symbol)
        val price = tokenData?.let { userLocalRepository.getPriceByTokenId(it.coingeckoId) }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }

    fun findTokenDataByAddress(mintAddress: String): Token? = userLocalRepository.findTokenByMint(mintAddress)

    suspend fun addRecipient(searchResult: SearchResult, date: Date) {
        recipientsLocalRepository.addRecipient(searchResult, date)
    }

    suspend fun getRecipients(): List<SearchResult> = recipientsLocalRepository.getRecipients()
}
