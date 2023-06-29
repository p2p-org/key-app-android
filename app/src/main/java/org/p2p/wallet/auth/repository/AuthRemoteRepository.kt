package org.p2p.wallet.auth.repository

import java.security.SecureRandom
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.mnemoticgenerator.English
import org.p2p.wallet.utils.mnemoticgenerator.MnemonicGenerator
import org.p2p.wallet.utils.mnemoticgenerator.Words

private const val ACCOUNTS_QUANTITY_TO_CREATE = 5

class AuthRemoteRepository(
    private val dispatchers: CoroutineDispatchers
) : AuthRepository {

    override suspend fun createAccount(
        path: DerivationPath,
        keys: List<String>,
        walletIndex: Int
    ): Account = withContext(dispatchers.io) {
        when (path) {
            DerivationPath.BIP32DEPRECATED -> Account.fromBip32Mnemonic(words = keys, walletIndex = walletIndex)
            else -> Account.fromBip44Mnemonic(words = keys, walletIndex = walletIndex, derivationPath = path)
        }
    }

    override suspend fun getDerivableAccounts(
        path: DerivationPath,
        keys: List<String>
    ): Map<DerivationPath, List<Account>> = withContext(dispatchers.io) {
        val data = mutableMapOf<DerivationPath, List<Account>>()
        val accounts: List<Deferred<Account>> =
            (0 until ACCOUNTS_QUANTITY_TO_CREATE).map { walletIndex ->
                async { createAccountWithIndex(path, walletIndex, keys) }
            }

        data[path] = accounts.awaitAll()
        data
    }

    private fun createAccountWithIndex(path: DerivationPath, walletIndex: Int, seedPhrase: List<String>): Account =
        when (path) {
            DerivationPath.BIP32DEPRECATED -> Account.fromBip32Mnemonic(seedPhrase, walletIndex)
            else -> Account.fromBip44Mnemonic(seedPhrase, walletIndex, path)
        }

    override suspend fun generatePhrase(): List<String> = withContext(dispatchers.computation) {
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWENTY_FOUR.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append)
        sb.toString().split(" ")
    }
}
