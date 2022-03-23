package org.p2p.wallet.user.repository

import org.koin.ext.getFullName
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import timber.log.Timber

class UserAccountRemoteRepository(
    private val rpcRepository: RpcAccountRepository
) : UserAccountRepository {

    private val cache = mutableMapOf<String, AccountInfo>()

    override suspend fun getAccountInfo(account: String, useCache: Boolean): AccountInfo? {
        val cachedValue = cache[account]
        if (useCache && cachedValue != null) {
            Timber.tag(UserAccountRepository::class.getFullName()).d("Getting from cache: $account")
            return cachedValue
        }

        val accountInfo = rpcRepository.getAccountInfo(account)
        if (accountInfo != null) cache[account] = accountInfo
        return accountInfo
    }
}
