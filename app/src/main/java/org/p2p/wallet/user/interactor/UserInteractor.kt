package org.p2p.wallet.user.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.core.token.Token
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.home.ui.main.TOKENS_VALID_FOR_BUY
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRepository
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val KEY_HIDDEN_TOKENS_VISIBILITY = "KEY_HIDDEN_TOKENS_VISIBILITY"

class UserInteractor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: HomeLocalRepository,
    private val rpcRepository: RpcBalanceRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences
) {

    fun findTokenData(mintAddress: String): Token? {
        val tokenData = userLocalRepository.findTokenData(mintAddress)
        val price = tokenData?.let { userLocalRepository.getPriceByToken(it.symbol) }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }

    fun getUserTokensFlow(): Flow<List<Token.Active>> =
        mainLocalRepository.getTokensFlow()
            .map { it.sortedWith(TokenComparator()) }

    suspend fun getSingleTokenForBuy(availableTokensSymbols: List<String> = TOKENS_VALID_FOR_BUY): Token? =
        getTokensForBuy(availableTokensSymbols).firstOrNull()

    suspend fun getTokensForBuy(
        availableTokensSymbols: List<String> = TOKENS_VALID_FOR_BUY
    ): List<Token> {
        val userTokens = getUserTokens()
        val allTokens = availableTokensSymbols.mapNotNull { tokenSymbol ->
            val userToken = userTokens.find { it.tokenSymbol == tokenSymbol }
            userToken ?: findTokenDataBySymbol(tokenSymbol)
        }

        return allTokens
    }

    suspend fun getBalance(address: Base58String): Long = rpcRepository.getBalance(address.base58Value)

    suspend fun loadAllTokensData() {
        val data = userRepository.loadAllTokens()
        userLocalRepository.setTokenData(data)
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

    suspend fun loadUserTokensAndUpdateLocal(fetchPrices: Boolean): List<Token.Active> {
        val newTokens = userRepository.loadUserTokens(tokenKeyProvider.publicKey, fetchPrices)
        val cachedTokens = mainLocalRepository.getUserTokens()

        updateLocalTokens(cachedTokens, newTokens)
        return getUserTokens()
    }

    private suspend fun updateLocalTokens(cachedTokens: List<Token.Active>, newTokens: List<Token.Active>) {
        val newTokensToCache = newTokens.map { newToken ->
            val oldToken = cachedTokens.find { oldToken -> oldToken.publicKey == newToken.publicKey }
            newToken.copy(visibility = oldToken?.visibility ?: newToken.visibility)
        }
        mainLocalRepository.clear()
        mainLocalRepository.updateTokens(newTokensToCache)
    }

    suspend fun getUserTokens(): List<Token.Active> =
        mainLocalRepository.getUserTokens()
            .sortedWith(TokenComparator())

    suspend fun getUserSolToken(): Token.Active? =
        mainLocalRepository.getUserTokens().find { it.isSOL }

    suspend fun findUserToken(mintAddress: String): Token.Active? =
        mainLocalRepository.getUserTokens().find { it.mintAddress == mintAddress }

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        mainLocalRepository.setTokenHidden(mintAddress, visibility)

    fun findMultipleTokenData(tokenSymbols: List<String>): List<Token> =
        tokenSymbols.mapNotNull { findTokenDataBySymbol(it) }

    private fun findTokenDataBySymbol(symbol: String): Token? {
        val tokenData = userLocalRepository.findTokenDataBySymbol(symbol)
        val price = tokenData?.let { userLocalRepository.getPriceByToken(it.symbol) }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }
}
