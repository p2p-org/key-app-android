package org.p2p.wallet.rpc.repository.account

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.AccountInfoParsed
import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.TokenAccounts

interface RpcAccountRepository {
    suspend fun getAccountInfoParsed(account: String, useCache: Boolean = true): AccountInfoParsed?
    suspend fun getAccountInfo(account: String): AccountInfo?
    suspend fun getAccountsInfo(accounts: List<String>): List<Pair<String, AccountInfo>>
    suspend fun getProgramAccounts(publicKey: PublicKey, config: RequestConfiguration): List<ProgramAccount>
    suspend fun getTokenAccountsByOwner(owner: PublicKey): TokenAccounts
    suspend fun getMultipleAccounts(publicKeys: List<PublicKey>): MultipleAccountsInfo
    suspend fun getPools(account: PublicKey): List<Pool.PoolInfo>
}
