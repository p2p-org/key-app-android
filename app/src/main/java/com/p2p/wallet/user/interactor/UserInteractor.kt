package com.p2p.wallet.user.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import com.p2p.wallet.common.crypto.Base58Utils
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.api.AllWallets
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import org.p2p.solanaj.core.PublicKey

private const val KEY_PHRASES = "KEY_PHRASES"

class UserInteractor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: MainLocalRepository,
    private val tokenProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun createAndSaveAccount(keys: List<String>) {
        val account = userRepository.createAccount(keys)
        tokenProvider.secretKey = account.secretKey
        tokenProvider.publicKey = Base58Utils.encode(account.publicKey.toByteArray())

        sharedPreferences.edit {
            putString(KEY_PHRASES, keys.joinToString(","))
        }
    }

    suspend fun loadTokenPrices(targetCurrency: String) {
        val tokens = AllWallets.getWalletConstList().map { it.tokenSymbol }
        val prices = userRepository.loadTokensPrices(tokens, targetCurrency)
        userLocalRepository.setTokenPrices(prices)
    }

    suspend fun loadTokenBids() {
        val tokens = AllWallets.getWalletConstList().map { it.tokenSymbol }
        val bids = userRepository.loadTokenBids(tokens)
        userLocalRepository.setTokenBids(bids)
    }

    suspend fun loadTokens() {
        val newTokens = userRepository.loadTokens()
        val hiddenTokens = mainLocalRepository.getTokens().filter { it.isHidden }.map { it.publicKey }

        if (hiddenTokens.isEmpty()) {
            mainLocalRepository.setTokens(newTokens)
            return
        }

        /* Starting check if token was previously hidden4 */
        val result = newTokens + newTokens.map { token ->
            val old = hiddenTokens.firstOrNull { it == token.publicKey }
            if (old != null) token.copy(isHidden = true) else token
        }

        mainLocalRepository.setTokens(result)
    }

    fun getTokensFlow(): Flow<List<Token>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getTokens(): List<Token> =
        mainLocalRepository.getTokens()

    suspend fun setTokenHidden(publicKey: String, isHidden: Boolean) =
        mainLocalRepository.setTokenHidden(publicKey, isHidden)

    suspend fun getPriceByToken(source: String, destination: String): Double =
        userRepository.getRate(source, destination)

    suspend fun clearMemoryData() {
        userLocalRepository.setTokenPrices(emptyList())
        mainLocalRepository.setTokens(emptyList())
    }

    suspend fun findAccountAddress(mintAddress: PublicKey) =
        mainLocalRepository.getTokens().firstOrNull {
            it.getFormattedMintAddress() == mintAddress.toString()
        }

    fun getSecretKeys(): List<String> =
        sharedPreferences.getString(KEY_PHRASES, "").orEmpty().split(",")
}