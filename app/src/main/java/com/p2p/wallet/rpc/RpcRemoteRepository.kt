package com.p2p.wallet.rpc

import android.util.Base64
import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParsed
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionRequest
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcSendTransactionConfig
import org.p2p.solanaj.model.types.SignatureInformation
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenAccounts
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.rpc.Environment
import java.util.HashMap

class RpcRemoteRepository(
    private val serumApi: RpcApi,
    private val mainnetApi: RpcApi,
    private val datahubApi: RpcApi,
    environmentManager: EnvironmentManager
) : RpcRepository {

    private var rpcApi: RpcApi

    init {
        rpcApi = createRpcApi(environmentManager.loadEnvironment())
        environmentManager.setOnEnvironmentListener { rpcApi = createRpcApi(it) }
    }

    private fun createRpcApi(environment: Environment): RpcApi = when (environment) {
        Environment.PROJECT_SERUM -> serumApi
        Environment.MAINNET -> mainnetApi
        Environment.DATAHUB -> datahubApi
    }

    override suspend fun getTokenAccountBalance(account: PublicKey): TokenAccountBalance {
        val params = listOf(account.toString())
        val rpcRequest = RpcRequest("getTokenAccountBalance", params)
        return rpcApi.getTokenAccountBalance(rpcRequest)
    }

    override suspend fun getRecentBlockhash(): RecentBlockhash {
        val rpcRequest = RpcRequest("getRecentBlockhash", null)
        return rpcApi.getRecentBlockhash(rpcRequest)
    }

    override suspend fun sendTransaction(transaction: TransactionRequest): String {
        val serializedTransaction = transaction.serialize()

        val base64Trx = Base64
            .encodeToString(serializedTransaction, Base64.DEFAULT)
            .replace("\n", "")

        val params = mutableListOf<Any>()

        params.add(base64Trx)
        params.add(RpcSendTransactionConfig())

        val rpcRequest = RpcRequest("sendTransaction", params)
        return rpcApi.sendTransaction(rpcRequest)
    }

    override suspend fun getAccountInfo(account: PublicKey): AccountInfo {
        val params = listOf(
            account.toString(),
            RpcSendTransactionConfig()
        )
        val rpcRequest = RpcRequest("getAccountInfo", params)
        return rpcApi.getAccountInfo(rpcRequest)
    }

    override suspend fun getPools(account: PublicKey): List<Pool.PoolInfo> {
        val params = listOf(
            account.toString(),
            ConfigObjects.ProgramAccountConfig(RpcSendTransactionConfig.Encoding.base64)
        )
        val rpcRequest = RpcRequest("getProgramAccounts", params)
        val response = rpcApi.getProgramAccounts(rpcRequest)
        return response.map { Pool.PoolInfo.fromProgramAccount(it) }
    }

    override suspend fun getBalance(account: PublicKey): Long {
        val params = listOf(account.toString())
        val rpcRequest = RpcRequest("getBalance", params)
        return rpcApi.getBalance(rpcRequest).value
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
        return rpcApi.getTokenAccountsByOwner(rpcRequest)
    }

    override suspend fun getMinimumBalanceForRentExemption(dataLength: Long): Long {
        val params = listOf(dataLength)
        val rpcRequest = RpcRequest("getMinimumBalanceForRentExemption", params)
        return rpcApi.getMinimumBalanceForRentExemption(rpcRequest)
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

        return rpcApi.getMultipleAccounts(rpcRequest)
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
        return mainnetApi.getConfirmedSignaturesForAddress2(rpcRequest)
    }

    override suspend fun getConfirmedTransaction(signature: String): ConfirmedTransactionParsed {
        val encoding = mapOf("encoding" to "jsonParsed")
        val params = listOf(signature, encoding)

        val rpcRequest = RpcRequest("getConfirmedTransaction", params)
        return mainnetApi.getConfirmedTransaction(rpcRequest)
    }
}