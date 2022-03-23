package org.p2p.wallet.auth.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.utils.mnemoticgenerator.English
import org.p2p.wallet.utils.mnemoticgenerator.MnemonicGenerator
import org.p2p.wallet.utils.mnemoticgenerator.Words
import java.security.SecureRandom

class AuthRemoteRepository : AuthRepository {

    companion object {
        private const val ACCOUNTS_SIZE = 5
    }

    override suspend fun createAccount(path: DerivationPath, keys: List<String>): Account =
        withContext(Dispatchers.IO) {
            when (path) {
                DerivationPath.BIP32DEPRECATED -> Account.fromBip32Mnemonic(keys, 0)
                DerivationPath.BIP44 -> Account.fromBip44Mnemonic(keys, 0)
                DerivationPath.BIP44CHANGE -> Account.fromBip44MnemonicWithChange(keys, 0)
            }
        }

    override suspend fun getDerivableAccounts(
        path: DerivationPath,
        keys: List<String>
    ): MutableMap<DerivationPath, List<Account>> {
        val data = mutableMapOf<DerivationPath, List<Account>>()
        val accounts = mutableListOf<Account>()

        for (index in 0 until ACCOUNTS_SIZE) {
            val account = when (path) {
                DerivationPath.BIP44 -> Account.fromBip44Mnemonic(keys, index)
                DerivationPath.BIP44CHANGE -> Account.fromBip44MnemonicWithChange(keys, index)
                DerivationPath.BIP32DEPRECATED -> Account.fromBip32Mnemonic(keys, index)
            }

            accounts.add(account)
        }

        data[path] = accounts
        return data
    }

    override suspend fun generatePhrase(): List<String> = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWENTY_FOUR.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append)
        sb.toString().split(" ")
    }
}
