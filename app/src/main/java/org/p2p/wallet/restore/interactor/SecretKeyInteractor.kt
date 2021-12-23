package org.p2p.wallet.restore.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.model.SeedPhraseResult
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.mnemoticgenerator.English
import org.p2p.wallet.utils.toPowerValue
import java.math.BigDecimal

private const val KEY_PHRASES = "KEY_PHRASES"
private const val KEY_DERIVATION_PATH = "KEY_DERIVATION_PATH"

class SecretKeyInteractor(
    private val authRepository: AuthRepository,
    private val userLocalRepository: UserLocalRepository,
    private val rpcRepository: RpcRepository,
    private val tokenProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences,
    private val usernameInteractor: UsernameInteractor,
) {

    suspend fun getDerivableAccounts(path: DerivationPath, keys: List<String>): List<DerivableAccount> {
        val derivableAccounts = authRepository.getDerivableAccounts(path, keys)
        val balanceAccounts = derivableAccounts.map { it.publicKey.toBase58() }
        val balances = rpcRepository.getBalances(balanceAccounts)
        return derivableAccounts.mapNotNull { account ->
            val balance = balances.find { it.first == account.publicKey.toBase58() }?.second ?: return@mapNotNull null
            val tokenData = userLocalRepository.findTokenDataBySymbol(Token.WRAPPED_SOL_MINT) ?: return@mapNotNull null

            val exchangeRate = userLocalRepository.getPriceByToken(tokenData.symbol)?.price ?: BigDecimal.ZERO
            val total = BigDecimal(balance).divide(tokenData.decimals.toPowerValue())
            DerivableAccount(path, account, total, total.multiply(exchangeRate))
        }
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