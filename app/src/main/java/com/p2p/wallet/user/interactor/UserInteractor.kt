package com.p2p.wallet.user.interactor

import com.p2p.wallet.common.crypto.Base58Utils
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.user.repository.UserRepository
import com.p2p.wallet.utils.WalletDataConst
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

class UserInteractor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: MainLocalRepository,
    private val tokenProvider: TokenKeyProvider
) {

    suspend fun createAndSaveAccount(keys: List<String>) {
        val account = userRepository.createAccount(keys)
        tokenProvider.secretKey = account.secretKey
        tokenProvider.publicKey = Base58Utils.encode(account.publicKey.toByteArray())
    }

    suspend fun loadTokens() {
        val tokens = userRepository.loadTokens()
        mainLocalRepository.setTokens(tokens)
    }

    suspend fun loadTokenPrices(targetCurrency: String) {
        val tokens = WalletDataConst.getWalletConstList().map { it.tokenSymbol }
        val prices = userRepository.loadTokensPrices(tokens, targetCurrency)
        userLocalRepository.setTokenPrices(prices)
    }

    suspend fun getTokensFlow(): Flow<List<Token>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getTokens(): List<Token> =
        mainLocalRepository.getTokens()

    suspend fun getPriceByToken(source: String, destination: String): BigDecimal =
        userRepository.getRate(source, destination)
}