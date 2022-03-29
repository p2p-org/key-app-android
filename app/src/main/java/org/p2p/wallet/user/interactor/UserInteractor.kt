package org.p2p.wallet.user.interactor

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.home.model.TokenConverter
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

    suspend fun getBalance(address: String) = rpcRepository.getBalance(address)

    suspend fun loadAllTokensData() {
        val data = userRepository.loadAllTokens()
        userLocalRepository.setTokenData(data)
    }

    fun fetchTokens(searchText: String = emptyString(), count: Int, refresh: Boolean) {
        userLocalRepository.fetchTokens(searchText, count, refresh)
    }

    fun getTokenListFlow() = userLocalRepository.getTokenListFlow()

    suspend fun loadUserTokensAndUpdateData() {
        val publicKey = tokenKeyProvider.publicKey
        val newTokens = userRepository.loadUserTokens(publicKey)

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

    private fun findTokenDataBySymbol(symbol: String): Token? {
        val tokenData = userLocalRepository.findTokenDataBySymbol(symbol)
        val price = tokenData?.let { userLocalRepository.getPriceByToken(it.symbol) }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }
}
