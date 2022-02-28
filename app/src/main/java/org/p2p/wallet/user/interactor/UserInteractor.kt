package org.p2p.wallet.user.interactor

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.home.api.TokenSymbols
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRepository

class UserInteractor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: HomeLocalRepository,
    private val rpcRepository: RpcRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    fun findTokenData(mintAddress: String): Token? {
        val tokenData = userLocalRepository.findTokenData(mintAddress)
        val price = tokenData?.let { userLocalRepository.getPriceByToken(it.symbol) }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }

    fun getUserTokensFlow(): Flow<List<Token.Active>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getBalance(address: String) = rpcRepository.getBalance(address)

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

    fun fetchTokens(searchText: String = "", count: Int, refresh: Boolean) {
        userLocalRepository.fetchTokens(searchText, count, refresh)
    }

    fun getTokenListFlow() = userLocalRepository.getTokenListFlow()

    suspend fun loadUserTokensAndUpdateData() {
        val publicKey = tokenKeyProvider.publicKey
        val newTokens = userRepository.loadTokens(publicKey)

        val oldTokens = mainLocalRepository.getUserTokens()
        mainLocalRepository.clear()
        val result = newTokens.map { token ->
            val old = oldTokens.find { it.publicKey == token.publicKey }
            if (old != null) {
                token.copy(visibility = old.visibility)
            } else {
                token
            }
        }

        mainLocalRepository.updateTokens(result)
    }

    suspend fun getUserTokens(): List<Token.Active> =
        mainLocalRepository.getUserTokens()
            .sortedWith(TokenComparator())

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        mainLocalRepository.setTokenHidden(mintAddress, visibility)

    suspend fun getPriceByToken(sourceSymbol: String, destinationSymbol: String): TokenPrice? =
        userRepository.getRate(sourceSymbol, destinationSymbol)
}