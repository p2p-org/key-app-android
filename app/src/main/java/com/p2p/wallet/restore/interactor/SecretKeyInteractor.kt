package com.p2p.wallet.restore.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import com.p2p.wallet.R
import com.p2p.wallet.auth.repository.AuthRepository
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.restore.model.DerivableAccount
import com.p2p.wallet.restore.model.SecretKey
import com.p2p.wallet.restore.model.SeedPhraseResult
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.utils.mnemoticgenerator.English
import com.p2p.wallet.utils.toPowerValue
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.solanaj.utils.crypto.Base58Utils
import java.math.BigDecimal

private const val KEY_PHRASES = "KEY_PHRASES"
private const val KEY_DERIVATION_PATH = "KEY_DERIVATION_PATH"

class SecretKeyInteractor(
    private val authRepository: AuthRepository,
    private val userLocalRepository: UserLocalRepository,
    private val rpcRepository: RpcRepository,
    private val tokenProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun getDerivableAccounts(path: DerivationPath, keys: List<String>): List<DerivableAccount> =
        authRepository.getDerivableAccounts(path, keys).mapNotNull { account ->
            val balance = rpcRepository.getBalance(account.publicKey.toBase58())
            val tokenData = userLocalRepository.getTokenData(Token.WRAPPED_SOL_MINT) ?: return@mapNotNull null

            val exchangeRate = userLocalRepository.getPriceByToken(tokenData.symbol).price
            val total = BigDecimal(balance).divide(tokenData.decimals.toPowerValue())
            DerivableAccount(path, account, total, total.multiply(exchangeRate))
        }

    suspend fun createAndSaveAccount(path: DerivationPath, keys: List<String>) {
        val account = authRepository.createAccount(path, keys)
        tokenProvider.secretKey = account.secretKey
        tokenProvider.publicKey = Base58Utils.encode(account.publicKey.toByteArray())

        sharedPreferences.edit {
            putString(KEY_PHRASES, keys.joinToString(","))
            putString(KEY_DERIVATION_PATH, path.stringValue)
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