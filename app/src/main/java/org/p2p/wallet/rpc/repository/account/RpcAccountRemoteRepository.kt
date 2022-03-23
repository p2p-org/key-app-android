package org.p2p.wallet.rpc.repository.account

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcSendTransactionConfig
import org.p2p.solanaj.model.types.TokenAccounts
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.rpc.RpcConstants
import org.p2p.wallet.rpc.api.RpcAccountApi
import timber.log.Timber

class RpcAccountRemoteRepository(private val api: RpcAccountApi) : RpcAccountRepository {

    override suspend fun getAccountInfo(account: String): AccountInfo? =
        try {
            val params = listOf(
                account,
                RequestConfiguration(encoding = Encoding.BASE64.encoding)
            )
            val rpcRequest = RpcRequest("getAccountInfo", params)
            api.getAccountInfo(rpcRequest).result
        } catch (e: EmptyDataException) {
            Timber.w("`getAccountInfo` responded with empty data, returning null")
            null
        }

    override suspend fun getAccountsInfo(accounts: List<String>): List<Pair<String, AccountInfo>> {
        val requestsBatch = accounts.map {
            val params = listOf(it, RequestConfiguration(encoding = Encoding.BASE64.encoding))
            RpcRequest("getAccountInfo", params)
        }

        return try {
            api.getAccountsInfo(requestsBatch)
                .mapIndexed { index, response ->
                    val key = (requestsBatch[index].params?.firstOrNull() as String)
                    val value = response.result
                    key to value
                }
        } catch (e: EmptyDataException) {
            Timber.w("`getAccountsInfo` responded with empty data, returning null")
            emptyList()
        }
    }

    override suspend fun getProgramAccounts(
        publicKey: PublicKey,
        config: RequestConfiguration
    ): List<ProgramAccount> = try {
        val params = listOf(publicKey.toString(), config)
        val rpcRequest = RpcRequest("getProgramAccounts", params)
        val response = api.getProgramAccounts(rpcRequest)

        // sometimes result can be null
        response.result
    } catch (e: EmptyDataException) {
        Timber.w("`getProgramAccounts` responded with empty data, returning empty list")
        emptyList()
    }

    override suspend fun getTokenAccountsByOwner(owner: String): TokenAccounts {
        val programId = TokenProgram.PROGRAM_ID
        val programIdParam = HashMap<String, String>()
        programIdParam["programId"] = programId.toBase58()

        val encoding = HashMap<String, String>()
        encoding[RpcConstants.REQUEST_PARAMETER_KEY_ENCODING] = RpcConstants.REQUEST_PARAMETER_VALUE_JSON_PARSED

        val params = listOf(
            owner,
            programIdParam,
            encoding
        )

        val rpcRequest = RpcRequest("getTokenAccountsByOwner", params)
        return api.getTokenAccountsByOwner(rpcRequest = rpcRequest).result
    }

    override suspend fun getMultipleAccounts(publicKeys: List<PublicKey>): MultipleAccountsInfo {
        val keys = publicKeys.map { it.toBase58() }

        val encoding = HashMap<String, String>()
        encoding[RpcConstants.REQUEST_PARAMETER_KEY_ENCODING] = RpcConstants.REQUEST_PARAMETER_VALUE_JSON_PARSED

        val params = listOf(
            keys,
            encoding
        )

        val rpcRequest = RpcRequest("getMultipleAccounts", params)

        return api.getMultipleAccounts(rpcRequest).result
    }

    override suspend fun getPools(account: PublicKey): List<Pool.PoolInfo> {
        val params = listOf(
            account.toString(),
            ConfigObjects.ProgramAccountConfig(RpcSendTransactionConfig.Encoding.base64)
        )
        val rpcRequest = RpcRequest("getProgramAccounts", params)
        val response = api.getProgramAccounts(rpcRequest).result
        return response.map { Pool.PoolInfo.fromProgramAccount(it) }
    }
}
