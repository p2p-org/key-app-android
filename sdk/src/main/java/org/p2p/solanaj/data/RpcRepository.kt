package org.p2p.solanaj.data

import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionRequest
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfirmedTransaction
import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.SignatureInformation
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenAccounts

interface RpcRepository {
    suspend fun getTokenAccountBalance(account: PublicKey): TokenAccountBalance
    suspend fun getRecentBlockhash(): RecentBlockhash
    suspend fun sendTransaction(
        recentBlockhash: RecentBlockhash,
        transaction: TransactionRequest,
        signers: List<Account>
    ): String

    suspend fun getAccountInfo(account: PublicKey): AccountInfo
    suspend fun getPools(account: PublicKey): List<Pool.PoolInfo>
    suspend fun getBalance(account: PublicKey): Long
    suspend fun getTokenAccountsByOwner(owner: PublicKey): TokenAccounts
    suspend fun getConfirmedSignaturesForAddress2(account: PublicKey, limit: Int): List<SignatureInformation>
    suspend fun getConfirmedTransaction(signature: String): ConfirmedTransaction
    suspend fun getBlockTime(block: Long): Long
    suspend fun getMinimumBalanceForRentExemption(dataLength: Long): Long
    suspend fun getMultipleAccounts(publicKeys: List<PublicKey>): MultipleAccountsInfo
}