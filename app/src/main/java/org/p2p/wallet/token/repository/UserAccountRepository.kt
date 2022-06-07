package org.p2p.wallet.token.repository

import org.p2p.solanaj.model.types.AccountInfo

interface UserAccountRepository {
    suspend fun getAccountInfo(account: String, useCache: Boolean = true): AccountInfo?
}
