package org.p2p.wallet.rpc.repository

import android.util.Base64
import org.p2p.wallet.infrastructure.network.EmptyDataException
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.rpc.api.RpcApi
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParsed
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcSendTransactionConfig
import org.p2p.solanaj.model.types.SignatureInformation
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenAccounts
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.rpc.Environment
import timber.log.Timber
import java.math.BigInteger

class RpcRemoteRepository(
    private val serumApi: RpcApi,
    private val mainnetApi: RpcApi,
    private val rpcpoolRpcApi: RpcApi,
    private val testnetApi: RpcApi,
    environmentManager: EnvironmentManager,
    onlyMainnet: Boolean = false
) : RpcRepository {

    private var rpcApi: RpcApi

    init {
        if (onlyMainnet) {
            rpcApi = mainnetApi
        } else {
            rpcApi = createRpcApi(environmentManager.loadEnvironment())
            environmentManager.setOnEnvironmentListener { rpcApi = createRpcApi(it) }
        }
    }

    private fun createRpcApi(environment: Environment): RpcApi = when (environment) {
        Environment.SOLANA -> serumApi
        Environment.RPC_POOL -> rpcpoolRpcApi
        Environment.MAINNET -> mainnetApi
        Environment.DEVNET -> testnetApi
    }

    override suspend fun getRecentBlockhash(): RecentBlockhash {
        val rpcRequest = RpcRequest("getRecentBlockhash", null)
        return rpcApi.getRecentBlockhash(rpcRequest).result
    }

    override suspend fun sendTransaction(transaction: Transaction): String {
        val serializedTransaction = transaction.serialize()

        val base64Trx = Base64
            .encodeToString(serializedTransaction, Base64.DEFAULT)
            .replace("\n", "")

        val params = mutableListOf<Any>()

        params.add(base64Trx)
        params.add(RequestConfiguration(encoding = Encoding.BASE64))

        val rpcRequest = RpcRequest("sendTransaction", params)
        return rpcApi.sendTransaction(rpcRequest).result
    }

    override suspend fun sendTransaction(serializedTransaction: String): String {
        val base64Trx = serializedTransaction.replace("\n", "")

        val params = mutableListOf<Any>()
        params.add(base64Trx)
        params.add(RequestConfiguration(encoding = Encoding.BASE64))

        val rpcRequest = RpcRequest("sendTransaction", params)
        return rpcApi.sendTransaction(rpcRequest).result
    }

    override suspend fun simulateTransaction(serializedTransaction: String): String {
        val base64Trx = serializedTransaction.replace("\n", "")

        val params = mutableListOf<Any>()
        params.add(base64Trx)
        params.add(RequestConfiguration(encoding = Encoding.BASE64))

        val rpcRequest = RpcRequest("simulateTransaction", params)
        return rpcApi.simulateTransaction(rpcRequest).result.logs?.firstOrNull().orEmpty()
    }

    override suspend fun getFees(commitment: String?): BigInteger {
        val params = commitment?.let {
            val config = RequestConfiguration(commitment = it)
            listOf<Any>(config)
        }

        val rpcRequest = RpcRequest("getFees", params)

        val response = rpcApi.getFees(rpcRequest).result
        return BigInteger.valueOf(response.value.feeCalculator.lamportsPerSignature)
    }

    override suspend fun getTokenAccountBalance(account: PublicKey): TokenAccountBalance {
        val params = listOf(account.toString())
        val rpcRequest = RpcRequest("getTokenAccountBalance", params)
        return rpcApi.getTokenAccountBalance(rpcRequest).result
    }

    override suspend fun getPools(account: PublicKey): List<Pool.PoolInfo> {
        val params = listOf(
            account.toString(),
            ConfigObjects.ProgramAccountConfig(RpcSendTransactionConfig.Encoding.base64)
        )
        val rpcRequest = RpcRequest("getProgramAccounts", params)
        val response = rpcApi.getProgramAccounts(rpcRequest).result
        return response.map { Pool.PoolInfo.fromProgramAccount(it) }
    }

    override suspend fun getAccountInfo(account: PublicKey): AccountInfo? {
        return try {
            val params = listOf(
                account.toString(),
                RequestConfiguration(encoding = Encoding.BASE64)
            )
            val rpcRequest = RpcRequest("getAccountInfo", params)
            rpcApi.getAccountInfo(rpcRequest).result
        } catch (e: EmptyDataException) {
            Timber.w("`getAccountInfo` responded with empty data, returning null")
            null
        }
    }

    override suspend fun getProgramAccounts(
        publicKey: PublicKey,
        config: RequestConfiguration
    ): List<ProgramAccount> {
        return try {
            val params = listOf(publicKey.toString(), config)
            val rpcRequest = RpcRequest("getProgramAccounts", params)
            val response = rpcApi.getProgramAccounts(rpcRequest)

            // sometimes result can be null
            response.result ?: emptyList()
        } catch (e: EmptyDataException) {
            Timber.w("`getProgramAccounts` responded with empty data, returning empty list")
            emptyList()
        }
    }

    override suspend fun getBalance(account: String): Long {
        val params = listOf(account)
        val rpcRequest = RpcRequest("getBalance", params)
        return rpcApi.getBalance(rpcRequest).result.value
    }

    override suspend fun getTokenAccountsByOwner(owner: PublicKey): TokenAccounts {
        val programId = TokenProgram.PROGRAM_ID
        val programIdParam = HashMap<String, String>()
        programIdParam["programId"] = programId.toBase58()

        val encoding = HashMap<String, String>()
        encoding["encoding"] = "jsonParsed"

        val params = listOf(
            owner.toBase58(),
            programIdParam,
            encoding
        )

        val rpcRequest = RpcRequest("getTokenAccountsByOwner", params)
        return rpcApi.getTokenAccountsByOwner(rpcRequest = rpcRequest).result
    }

    override suspend fun getMinimumBalanceForRentExemption(dataLength: Long): Long {
        val params = listOf(dataLength)
        val rpcRequest = RpcRequest("getMinimumBalanceForRentExemption", params)
        return rpcApi.getMinimumBalanceForRentExemption(rpcRequest).result
    }

    override suspend fun getMultipleAccounts(publicKeys: List<PublicKey>): MultipleAccountsInfo {
        val keys = publicKeys.map { it.toBase58() }

        val encoding = HashMap<String, String>()
        encoding["encoding"] = "jsonParsed"

        val params = listOf(
            keys,
            encoding
        )

        val rpcRequest = RpcRequest("getMultipleAccounts", params)

        return rpcApi.getMultipleAccounts(rpcRequest).result
    }

    /**
     * The history is being fetched from main-net despite the selected network
     * */
    override suspend fun getConfirmedSignaturesForAddress(
        account: PublicKey,
        before: String?,
        limit: Int
    ): List<SignatureInformation> {
        val params = listOf(
            account.toString(),
            ConfigObjects.ConfirmedSignFAddr2(before, limit)
        )

        val rpcRequest = RpcRequest("getConfirmedSignaturesForAddress2", params)
        return mainnetApi.getConfirmedSignaturesForAddress2(rpcRequest).result
    }

    override suspend fun getConfirmedTransactions(
        signatures: List<String>
    ): List<ConfirmedTransactionParsed> {
        val requestsBatch = signatures.map {
            val encoding = mapOf("encoding" to "jsonParsed")
            val params = listOf(it, encoding)

            RpcRequest("getConfirmedTransaction", params)
        }

        return mainnetApi.getConfirmedTransactions(requestsBatch).map { it.result }
    }
}