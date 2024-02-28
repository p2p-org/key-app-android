package org.p2p.wallet.restore.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.SOL_DECIMALS
import org.p2p.core.utils.fromLamports
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.toBase58Instance
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.restore.model.SeedPhraseVerifyResult
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.utils.mnemoticgenerator.English

// duck-taped, extract to storage some day
const val KEY_IS_AUTH_BY_SEED_PHRASE = "KEY_IS_AUTH_BY_SEED_PHRASE"

class SeedPhraseInteractor(
    private val authRepository: AuthRepository,
    private val rpcRepository: RpcBalanceRepository,
    private val tokenProvider: TokenKeyProvider,
    private val usernameInteractor: UsernameInteractor,
    private val dispatchers: CoroutineDispatchers,
    private val adminAnalytics: AdminAnalytics,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun getDerivableAccounts(keys: List<String>): List<DerivableAccount> = withContext(dispatchers.io) {
        val paths = listOf(DerivationPath.BIP44CHANGE, DerivationPath.BIP44, DerivationPath.BIP32DEPRECATED)
        val derivableAccounts = mutableMapOf<DerivationPath, List<Account>>()
        paths.forEach { path ->
            val data: Map<DerivationPath, List<Account>> = authRepository.getDerivableAccounts(path, keys)
            derivableAccounts += data
        }

        val balances = loadBalances(derivableAccounts.values.toList())

        /* Map derivable accounts with balances */
        val result: List<DerivableAccount> = derivableAccounts.flatMap { (path, accounts) ->
            mapDerivableAccounts(accounts, balances, path)
        }

        result
    }

    private suspend fun loadBalances(derivableAccounts: List<List<Account>>): List<Pair<String, BigInteger>> {
        val accountToGetBalance: List<Base58String> = derivableAccounts.flatten()
            .map { it.publicKey.toBase58Instance() }
        if (accountToGetBalance.isEmpty()) {
            return emptyList()
        }

        return rpcRepository.getBalances(accountToGetBalance.map(Base58String::base58Value))
    }

    private fun mapDerivableAccounts(
        accounts: List<Account>,
        balances: List<Pair<String, BigInteger>>,
        path: DerivationPath
    ): List<DerivableAccount> = accounts.mapNotNull { account ->
        val solBalance = balances.find { it.first == account.publicKey.toBase58() }
            ?.second
            ?: return@mapNotNull null

        val totalSol = solBalance.fromLamports(SOL_DECIMALS)
        DerivableAccount(
            path = path,
            account = account,
            totalInSol = totalSol
        )
    }

    suspend fun createAndSaveAccount(
        path: DerivationPath,
        mnemonicPhrase: List<String>,
        walletIndex: Int
    ) {
        val account = authRepository.createAccount(path, mnemonicPhrase, walletIndex)
        val publicKey = account.publicKey.toBase58()

        tokenProvider.keyPair = account.keypair
        tokenProvider.publicKey = publicKey

        Timber.i("Account: $publicKey restored using $path")

        usernameInteractor.tryRestoreUsername(publicKey.toBase58Instance())
        adminAnalytics.logPasswordCreated()
    }

    @Deprecated("Old onboarding flow, delete someday")
    suspend fun generateSecretKeys(): List<String> = authRepository.generatePhrase()

    fun verifySeedPhrase(secretKeys: List<SeedPhraseWord>): SeedPhraseVerifyResult {
        val validWords = English.INSTANCE.words
        val seedWords = secretKeys.map { it.text }

        val validatedKeys = secretKeys.map { key ->
            val isValid = validWords.contains(key.text)
            key.copy(isValid = isValid)
        }

        val isInvalidSeedPhrase = validatedKeys.any { !it.isValid }
        if (isInvalidSeedPhrase) {
            setSeedPhraseAuthSuccess(isSuccess = false)
            return SeedPhraseVerifyResult.Invalid(validatedKeys)
        }

        return try {
            MnemonicCode.INSTANCE.check(seedWords)
            setSeedPhraseAuthSuccess(isSuccess = true)
            SeedPhraseVerifyResult.Verified(validatedKeys)
        } catch (checkError: MnemonicException) {
            Timber.i(checkError)
            setSeedPhraseAuthSuccess(isSuccess = false)
            SeedPhraseVerifyResult.VerificationFailed
        }
    }

    private fun setSeedPhraseAuthSuccess(isSuccess: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_IS_AUTH_BY_SEED_PHRASE, isSuccess)
        }
    }
}
