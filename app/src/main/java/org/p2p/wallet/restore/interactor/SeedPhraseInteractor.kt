package org.p2p.wallet.restore.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.utils.mnemoticgenerator.English
import org.p2p.core.utils.scaleLong
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.withContext
import org.p2p.core.utils.Constants.SOL_NAME

// duck-taped, extract to storage some day
const val KEY_IS_AUTH_BY_SEED_PHRASE = "KEY_IS_AUTH_BY_SEED_PHRASE"

class SeedPhraseInteractor(
    private val authRepository: AuthRepository,
    private val rpcRepository: RpcBalanceRepository,
    private val tokenProvider: TokenKeyProvider,
    private val usernameInteractor: UsernameInteractor,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val dispatchers: CoroutineDispatchers,
    private val adminAnalytics: AdminAnalytics,
    private val sharedPreferences: SharedPreferences
) {

    private var solRate: BigDecimal? = null

    suspend fun getDerivableAccounts(keys: List<String>): List<DerivableAccount> = withContext(dispatchers.io) {
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
        val balance = balances.find { it.first == account.publicKey.toBase58() }
            ?.second
            ?: return@mapNotNull null
        val tokenSymbol = TokenId(SOL_NAME.lowercase())

        val exchangeRate =
            solRate ?: tokenPricesRepository.getTokenPriceBySymbol(tokenSymbol, USD_READABLE_SYMBOL).price
        if (solRate == null) solRate = exchangeRate

        val total = balance.fromLamports().scaleLong()
        DerivableAccount(path, account, total, total.multiply(exchangeRate))
    }

    suspend fun createAndSaveAccount(
        path: DerivationPath,
        mnemonicPhrase: List<String>,
    ) {
        val account = authRepository.createAccount(path, mnemonicPhrase)
        val publicKey = account.publicKey.toBase58()

        tokenProvider.keyPair = account.keypair
        tokenProvider.publicKey = publicKey

        Timber.i("Account: $publicKey restored using $path")

        usernameInteractor.tryRestoreUsername(publicKey.toBase58Instance())
        adminAnalytics.logPasswordCreated()
    }

    suspend fun generateSecretKeys(): List<String> = authRepository.generatePhrase()

    sealed interface SeedPhraseVerifyResult {
        class VerifiedSeedPhrase(val seedPhraseWord: List<SeedPhraseWord>) : SeedPhraseVerifyResult
        object VerifyByChecksumFailed : SeedPhraseVerifyResult
    }

    fun verifySeedPhrase(secretKeys: List<SeedPhraseWord>): SeedPhraseVerifyResult {
        val validWords = English.INSTANCE.words
        val seedWords = secretKeys.map { it.text }

        val validatedKeys = secretKeys.map { key ->
            val isValid = validWords.contains(key.text)
            key.copy(isValid = isValid)
        }

        return if (validatedKeys.any { !it.isValid }) {
            updateSeedPhraseAuthFlag(isAuthSuccess = false)
            SeedPhraseVerifyResult.VerifiedSeedPhrase(validatedKeys)
        } else {
            try {
                MnemonicCode.INSTANCE.check(seedWords)
                updateSeedPhraseAuthFlag(isAuthSuccess = true)

                SeedPhraseVerifyResult.VerifiedSeedPhrase(validatedKeys)
            } catch (checkError: MnemonicException) {
                Timber.i(checkError)
                updateSeedPhraseAuthFlag(isAuthSuccess = false)
                SeedPhraseVerifyResult.VerifyByChecksumFailed
            }
        }
    }

    private fun updateSeedPhraseAuthFlag(isAuthSuccess: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_IS_AUTH_BY_SEED_PHRASE, isAuthSuccess)
        }
    }
}
