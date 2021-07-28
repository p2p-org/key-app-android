package com.p2p.wallet.auth.repository

import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.crypto.DerivationPath

interface AuthRepository {
    suspend fun getDerivableAccounts(path: DerivationPath, keys: List<String>): List<Account>
    suspend fun createAccount(path: DerivationPath, keys: List<String>): Account
    suspend fun generatePhrase(): List<String>
}