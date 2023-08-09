package org.p2p.wallet.rpc.repository.account

import timber.log.Timber
import org.p2p.core.network.data.EmptyDataException
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.TokenAccounts
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.rpc.RpcConstants
import org.p2p.wallet.rpc.api.RpcAccountApi

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
            Timber.i("`getAccountInfo` responded with empty data, returning null")
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

    override suspend fun getTokenAccountsByOwner(owner: PublicKey): TokenAccounts {
        require(owner.toBase58().isNotBlank()) { "Owner ID cannot be blank" }

        val programId = TokenProgram.PROGRAM_ID
        val programIdParam = HashMap<String, String>()
        programIdParam["programId"] = programId.toBase58()

        val config = RequestConfiguration(
            encoding = RpcConstants.REQUEST_PARAMETER_VALUE_JSON_PARSED,
            commitment = RpcConstants.REQUEST_PARAMETER_VALUE_CONFIRMED
        )

        val params = listOf(
            owner.toBase58(),
            programIdParam,
            config
        )

        val rpcRequest = RpcRequest("getTokenAccountsByOwner", params)
        return api.getTokenAccountsByOwner(rpcRequest = rpcRequest).result
    }

    override suspend fun getMultipleAccounts(publicKeys: List<PublicKey>): MultipleAccountsInfo {
        val keys = publicKeys.map { it.toBase58() }

        val config = RequestConfiguration(
            encoding = RpcConstants.REQUEST_PARAMETER_VALUE_JSON_PARSED,
        )

        val params = listOf(
            keys,
            config
        )

        val rpcRequest = RpcRequest("getMultipleAccounts", params)

        return api.getMultipleAccounts(rpcRequest).result
    }

    override suspend fun getPools(account: PublicKey): List<Pool.PoolInfo> {
        val params = listOf(
            account.toString(),
            ConfigObjects.ProgramAccountConfig(Encoding.BASE64)
        )
        val rpcRequest = RpcRequest("getProgramAccounts", params)
        val response = api.getProgramAccounts(rpcRequest).result
        return response.map { Pool.PoolInfo.fromProgramAccount(it) }
    }
}
