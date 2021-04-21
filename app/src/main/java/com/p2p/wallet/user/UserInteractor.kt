package com.p2p.wallet.user

import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.infrastructure.network.provider.PublicKeyProvider
import com.p2p.wallet.infrastructure.security.SecureStorage
import org.bitcoinj.core.Base58

private const val KEY_SECRET_KEY = "KEY_SECRET_KEY"

class UserInteractor(
    private val userRepository: UserRepository,
    private val secureStorage: SecureStorage,
    private val tokenProvider: PublicKeyProvider
) {

    suspend fun createAndSaveAccount(keys: List<String>) {
        val account = userRepository.createAccount(keys)
        val publicKey = Base58.encode(account.publicKey.toByteArray())
        val secretKey = Base58.encode(account.secretKey)

        secureStorage.saveString(KEY_SECRET_KEY, secretKey)

        tokenProvider.publicKey = publicKey
    }

    suspend fun loadTokens(targetCurrency: String): List<Token> =
        userRepository.loadTokens(targetCurrency)
}