package org.p2p.wallet.rpc.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParsed
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.SignatureInformation
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenAccounts
import java.math.BigInteger

interface RpcRepository {
    suspend fun getRecentBlockhash(): RecentBlockhash

    suspend fun sendTransaction(transaction: Transaction): String
    suspend fun simulateTransaction(transaction: Transaction): String

    suspend fun sendTransaction(serializedTransaction: String): String
    suspend fun simulateTransaction(serializedTransaction: String): String

    suspend fun getFees(commitment: String?): BigInteger
    suspend fun getTokenAccountBalances(accounts: List<String>): List<Pair<String, TokenAccountBalance>>
    suspend fun getTokenAccountBalance(account: PublicKey): TokenAccountBalance
    suspend fun getPools(account: PublicKey): List<Pool.PoolInfo>
    suspend fun getAccountInfo(account: String): AccountInfo?
    suspend fun getAccountsInfo(accounts: List<String>): List<Pair<String, AccountInfo>>
    suspend fun getProgramAccounts(
        publicKey: PublicKey,
        config: RequestConfiguration
    ): List<ProgramAccount>

    suspend fun getBalance(account: String): Long
    suspend fun getBalances(accounts: List<String>): List<Pair<String, Long>>
    suspend fun getTokenAccountsByOwner(owner: String): TokenAccounts
    suspend fun getMinimumBalanceForRentExemption(dataLength: Long): Long
    suspend fun getMultipleAccounts(publicKeys: List<PublicKey>): MultipleAccountsInfo

    /**
     * The history is being fetched from main-net despite the selected network
     * */
    suspend fun getConfirmedTransactions(signatures: List<String>): List<ConfirmedTransactionParsed>
    suspend fun getConfirmedSignaturesForAddress(
        account: PublicKey,
        before: String?,
        limit: Int
    ): List<SignatureInformation>
}