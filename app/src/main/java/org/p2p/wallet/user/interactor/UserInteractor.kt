package org.p2p.wallet.user.interactor

import org.p2p.wallet.main.api.TokenColors
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TokenConverter
import org.p2p.wallet.main.repository.MainLocalRepository
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import java.math.BigDecimal

class UserInteractor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: MainLocalRepository,
    private val rpcRepository: RpcRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    fun findTokenData(mintAddress: String): Token? {
        val tokenData = userLocalRepository.findTokenData(mintAddress)
        return tokenData?.let { TokenConverter.fromNetwork(tokenData) }
    }

    fun getUserTokensFlow(): Flow<List<Token.Active>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getBalance(address: String) = rpcRepository.getBalance(address)

    suspend fun loadTokenPrices(targetCurrency: String) {
        val tokens = TokenColors.getSymbols()
        val prices = userRepository.loadTokensPrices(tokens, targetCurrency)
        userLocalRepository.setTokenPrices(prices)
    }

    suspend fun loadAllTokensData() {
        val data = userRepository.loadAllTokens()
        userLocalRepository.setTokenData(data)
    }

    suspend fun loadUserTokensAndUpdateData() {
        val newTokens = userRepository.loadTokens()
        /* We have case when temporary SOL account is created but not deleted in database */
        val owner = tokenKeyProvider.publicKey
        mainLocalRepository.setHiddenSol(owner)
        mainLocalRepository.updateTokens(newTokens)
    }

    suspend fun getUserTokens(): List<Token.Active> =
        mainLocalRepository.getUserTokens()
            .sortedByDescending { it.totalInUsd }
            .sortedByDescending { it.isSOL }

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        mainLocalRepository.setTokenHidden(mintAddress, visibility)

    suspend fun getPriceByToken(sourceSymbol: String, destinationSymbol: String): BigDecimal =
        userRepository.getRate(sourceSymbol, destinationSymbol)

    suspend fun clearMemoryData() {
        mainLocalRepository.setTokens(emptyList())
    }

    suspend fun findAccountAddress(mintAddress: String): Token.Active? =
        mainLocalRepository.getUserTokens().firstOrNull {
            it.mintAddress == mintAddress
        }
}