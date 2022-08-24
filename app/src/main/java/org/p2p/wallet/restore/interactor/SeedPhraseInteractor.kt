package org.p2p.wallet.restore.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.user.repository.prices.TokenSymbol
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.mnemoticgenerator.English
import org.p2p.wallet.utils.scaleLong
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.withContext

private const val KEY_PHRASES = "KEY_PHRASES"
private const val KEY_DERIVATION_PATH = "KEY_DERIVATION_PATH"

class SeedPhraseInteractor(
    private val authRepository: AuthRepository,
    private val rpcRepository: RpcBalanceRepository,
    private val tokenProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences,
    private val usernameInteractor: UsernameInteractor,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val dispatchers: CoroutineDispatchers,
    private val adminAnalytics: AdminAnalytics
) {

    private var solRate: BigDecimal? = null

    suspend fun getDerivableAccounts(keys: List<String>): List<DerivableAccount> =
        withContext(dispatchers.io) {
            val paths = listOf(DerivationPath.BIP44CHANGE, DerivationPath.BIP44, DerivationPath.BIP32DEPRECATED)
            val derivableAccounts = mutableMapOf<DerivationPath, List<Account>>()
            paths.forEach { path ->
                val data = authRepository.getDerivableAccounts(path, keys)
                derivableAccounts += data
            }
            /* Loading balances */
            val balanceAccounts = derivableAccounts.values.flatten().map { it.publicKey.toBase58() }
            val balances = if (balanceAccounts.isNotEmpty()) rpcRepository.getBalances(balanceAccounts) else emptyList()

            /* Map derivable accounts with balances */
            val result: List<DerivableAccount> = derivableAccounts.flatMap { (path, accounts) ->
                mapDerivableAccounts(accounts, balances, path)
            }

            result
        }

    private suspend fun mapDerivableAccounts(
        accounts: List<Account>,
        balances: List<Pair<String, BigInteger>>,
        path: DerivationPath
    ): List<DerivableAccount> = accounts.mapNotNull { account ->
        val balance = balances.find { it.first == account.publicKey.toBase58() }?.second ?: return@mapNotNull null
        val tokenSymbol = TokenSymbol(SOL_SYMBOL)

        val exchangeRate =
            solRate ?: tokenPricesRepository.getTokenPriceBySymbol(tokenSymbol, USD_READABLE_SYMBOL).price
        if (solRate == null) solRate = exchangeRate

        val total = balance.fromLamports().scaleLong()
        DerivableAccount(path, account, total, total.multiply(exchangeRate))
    }

    suspend fun createAndSaveAccount(
        path: DerivationPath,
        mnemonicPhrase: List<String>,
        lookupForUsername: Boolean = true
    ) {
        val account = authRepository.createAccount(path, mnemonicPhrase)
        val publicKey = account.publicKey.toBase58()

        tokenProvider.secretKey = account.secretKey
        tokenProvider.publicKey = publicKey

        sharedPreferences.edit {
            putString(KEY_PHRASES, mnemonicPhrase.joinToString(separator = ","))
            putString(KEY_DERIVATION_PATH, path.stringValue)
        }

        if (lookupForUsername) {
            usernameInteractor.findUsernameByAddress(publicKey)
        }
        adminAnalytics.logPasswordCreated()
    }

    suspend fun generateSecretKeys(): List<String> =
        authRepository.generatePhrase()

    fun verifySeedPhrase(secretKeys: List<SeedPhraseKey>): List<SeedPhraseKey> {
        val words = English.INSTANCE.words
        val keys = secretKeys.filter { key -> key.text.isNotEmpty() }

        val validatedKeys = keys.map { key ->
            val isValid = words.contains(key.text)
            key.copy(isValid = isValid)
        }

        return validatedKeys
    }
}
