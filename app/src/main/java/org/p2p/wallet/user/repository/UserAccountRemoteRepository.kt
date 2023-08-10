package org.p2p.wallet.user.repository

import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository

class UserAccountRemoteRepository(
    private val rpcRepository: RpcAccountRepository
) : UserAccountRepository {

    override suspend fun getAccountInfo(account: String, useCache: Boolean): AccountInfo? {
        return rpcRepository.getAccountInfo(account)
    }
}
