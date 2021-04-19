package com.p2p.wallet.user

import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.infrastructure.network.PublicKeyProvider
import com.p2p.wallet.infrastructure.security.SecureStorage
import com.p2p.wallet.utils.WalletDataConst
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

    suspend fun loadBalance(): Long = userRepository.loadBalance()

    // fixme: workaround about this mapping, looks something strange, try removing hardcode
    suspend fun loadTokens(balance: Long): List<Token> {
        val accounts = userRepository.loadTokens()
        val wallets = WalletDataConst.getWalletConstList()

        return wallets.mapNotNull { wallet ->
            val account = accounts.find { it.mintAddress == wallet.mint } ?: return@mapNotNull null
            val decimals = userRepository.loadDecimals(account.mintAddress)
            Token(
                tokenSymbol = wallet.tokenSymbol,
                tokenName = wallet.tokenName,
                iconUrl = wallet.icon,
                depositAddress = account.depositAddress,
                mintAddress = account.mintAddress,
                price = account.getUSPrice(decimals, wallet.isUS()),
                amount = account.getAmount(decimals),
                decimals = decimals,
                walletBinds = if (wallet.isUS()) 1.0 else 0.0
            )
        } + Token.getSOL(tokenProvider.publicKey, balance)
    }
}