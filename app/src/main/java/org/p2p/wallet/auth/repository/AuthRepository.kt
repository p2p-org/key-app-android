package org.p2p.wallet.auth.repository

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath

interface AuthRepository {
    suspend fun getDerivableAccounts(
        path: DerivationPath,
        keys: List<String>
    ): Map<DerivationPath, List<Account>>

    suspend fun createAccount(path: DerivationPath, keys: List<String>, walletIndex: Int): Account
    suspend fun generatePhrase(): List<String>
}
