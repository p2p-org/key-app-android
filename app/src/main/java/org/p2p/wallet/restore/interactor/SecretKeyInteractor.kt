package org.p2p.wallet.restore.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.model.SeedPhraseResult
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.mnemoticgenerator.English
import org.p2p.wallet.utils.scaleLong
import java.math.BigDecimal
import java.math.BigInteger

private const val KEY_PHRASES = "KEY_PHRASES"
private const val KEY_DERIVATION_PATH = "KEY_DERIVATION_PATH"

class SecretKeyInteractor(
    private val authRepository: AuthRepository,
    private val userLocalRepository: UserLocalRepository,
    private val rpcRepository: RpcBalanceRepository,
    private val tokenProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences,
    private val usernameInteractor: UsernameInteractor,
    private val adminAnalytics: AdminAnalytics
) {

    suspend fun getDerivableAccounts(keys: List<String>): List<DerivableAccount> =
        withContext(Dispatchers.IO) {
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
            val result = derivableAccounts.flatMap { (path, accounts) ->
                mapDerivableAccounts(accounts, balances, path)
            }

            return@withContext result
        }

    private fun mapDerivableAccounts(
        accounts: List<Account>,
        balances: List<Pair<String, BigInteger>>,
        path: DerivationPath
    ) = accounts.mapNotNull { account ->
        val balance = balances.find { it.first == account.publicKey.toBase58() }?.second ?: return@mapNotNull null
        val exchangeRate = userLocalRepository.getPriceByToken(SOL_SYMBOL)?.price ?: BigDecimal.ZERO
        val total = balance.fromLamports().scaleLong()
        DerivableAccount(path, account, total, total.multiply(exchangeRate))
    }

    suspend fun createAndSaveAccount(path: DerivationPath, keys: List<String>, lookup: Boolean = true) {
        val account = authRepository.createAccount(path, keys)
        val publicKey = account.publicKey.toBase58()

        tokenProvider.secretKey = account.secretKey
        tokenProvider.publicKey = publicKey

        sharedPreferences.edit {
            putString(KEY_PHRASES, keys.joinToString(","))
            putString(KEY_DERIVATION_PATH, path.stringValue)
        }

        if (lookup) {
            usernameInteractor.lookupUsername(publicKey)
        }
        adminAnalytics.logPasswordCreated()
    }

    suspend fun generateSecretKeys(): List<String> =
        authRepository.generatePhrase()

    fun verifySeedPhrase(secretKeys: List<SecretKey>): SeedPhraseResult {
        val words = English.INSTANCE.words
        val data = secretKeys.map { it.text }.filter { it.isNotEmpty() }

        val wordNotFound = data.any { !words.contains(it) }

        return if (wordNotFound) {
            SeedPhraseResult.Error(R.string.auth_wrong_seed_phrase)
        } else {
            SeedPhraseResult.Success(secretKeys)
        }
    }
}
