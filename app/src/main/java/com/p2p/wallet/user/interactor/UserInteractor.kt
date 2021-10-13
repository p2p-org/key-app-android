package com.p2p.wallet.user.interactor

import com.p2p.wallet.main.api.TokenColors
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

class UserInteractor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: MainLocalRepository,
    private val rpcRepository: RpcRepository
) {

    suspend fun loadTokenPrices(targetCurrency: String) {
        val tokens = TokenColors.getSymbols()
        val prices = userRepository.loadTokensPrices(tokens, targetCurrency)
        userLocalRepository.setTokenPrices(prices)
    }

    fun findTokenData(mintAddress: String): Token? {
        val tokenData = userLocalRepository.findTokenData(mintAddress)
        return tokenData?.let { TokenConverter.fromNetwork(tokenData) }
    }

    suspend fun loadAllTokensData() {
        val data = userRepository.loadAllTokens()
        userLocalRepository.setTokenData(data)
    }

    suspend fun loadUserTokensAndUpdateData() {
        val newTokens = userRepository.loadTokens()
        mainLocalRepository.updateTokens(newTokens)
    }

    suspend fun getBalance(address: String) = rpcRepository.getBalance(address)

    fun getUserTokensFlow(): Flow<List<Token.Active>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getUserTokens(): List<Token.Active> =
        mainLocalRepository.getTokens()

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        mainLocalRepository.setTokenHidden(mintAddress, visibility)

    suspend fun getPriceByToken(sourceSymbol: String, destinationSymbol: String): BigDecimal =
        userRepository.getRate(sourceSymbol, destinationSymbol)

    suspend fun clearMemoryData() {
        mainLocalRepository.setTokens(emptyList())
    }

    suspend fun findAccountAddress(mintAddress: String): Token.Active? =
        mainLocalRepository.getTokens().firstOrNull {
            it.mintAddress == mintAddress
        }
}