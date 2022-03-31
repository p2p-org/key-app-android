package org.p2p.wallet.user.interactor

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.home.api.TokenSymbols
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRepository
import org.p2p.wallet.utils.emptyString

class UserInteractor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: HomeLocalRepository,
    private val rpcRepository: RpcBalanceRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    fun findTokenData(mintAddress: String): Token? {
        val tokenData = userLocalRepository.findTokenData(mintAddress)
        val price = tokenData?.let { userLocalRepository.getPriceByToken(it.symbol) }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }

    fun findTokenDataBySymbol(symbol: String): Token? {
        val tokenData = userLocalRepository.findTokenDataBySymbol(symbol)
        val price = tokenData?.let { userLocalRepository.getPriceByToken(it.symbol) }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }

    fun getUserTokensFlow(): Flow<List<Token.Active>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getTokensForBuy(availableTokensSymbols: List<String>): List<Token> {
        val userTokens = getUserTokens()
        val publicKey = tokenKeyProvider.publicKey
        val allTokens = availableTokensSymbols
            .mapNotNull { tokenSymbol ->
                val userToken = userTokens.find { it.tokenSymbol == tokenSymbol }
                return@mapNotNull when {
                    userToken != null ->
                        if (userToken.isSOL && userToken.publicKey != publicKey) null else userToken
                    else -> findTokenDataBySymbol(tokenSymbol)
                }
            }
            .sortedWith(TokenComparator())

        return allTokens
    }

    suspend fun getBalance(address: String): Long = rpcRepository.getBalance(address)

    suspend fun loadTokenPrices(targetCurrency: String) {
        // TODO: 15.02.2022 replace TokenSymbols with user tokens from local storage [P2PW-1315]
        val tokens = TokenSymbols.tokenSymbols()
        val prices = userRepository.loadTokensPrices(tokens, targetCurrency)
        userLocalRepository.setTokenPrices(prices)
    }

    suspend fun loadAllTokensData() {
        val data = userRepository.loadAllTokens()
        userLocalRepository.setTokenData(data)
    }

    fun fetchTokens(searchText: String = emptyString(), count: Int, refresh: Boolean) {
        userLocalRepository.fetchTokens(searchText, count, refresh)
    }

    fun getTokenListFlow() = userLocalRepository.getTokenListFlow()

    suspend fun loadUserTokensAndUpdateLocal() {
        val newTokens = userRepository.loadTokens(tokenKeyProvider.publicKey)
        val cachedTokens = mainLocalRepository.getUserTokens()

        updateLocalTokens(cachedTokens, newTokens)
    }

    private suspend fun updateLocalTokens(cachedTokens: List<Token.Active>, newTokens: List<Token.Active>) {
        mainLocalRepository.clear()
        val newTokensToCache = newTokens.map { newToken ->
            val oldToken = cachedTokens.find { oldToken -> oldToken.publicKey == newToken.publicKey }
            newToken.copy(visibility = oldToken?.visibility ?: newToken.visibility)
        }

        mainLocalRepository.updateTokens(newTokensToCache)
    }

    suspend fun getUserTokens(): List<Token.Active> =
        mainLocalRepository.getUserTokens()
            .sortedWith(TokenComparator())

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        mainLocalRepository.setTokenHidden(mintAddress, visibility)

    suspend fun getPriceByToken(sourceSymbol: String, destinationSymbol: String): TokenPrice? =
        userRepository.getRate(sourceSymbol, destinationSymbol)
}
