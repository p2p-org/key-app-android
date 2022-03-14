package org.p2p.wallet.rpc.repository.account

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.TokenAccounts
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.rpc.api.RpcApi
import timber.log.Timber

class RpcAccountRemoteRepository(private val rpcApi: RpcApi) : RpcAccountRepository {

    override suspend fun getAccountInfo(account: String): AccountInfo? {
        return try {
            val params = listOf(
                account,
                RequestConfiguration(encoding = Encoding.BASE64.encoding)
            )
            val rpcRequest = RpcRequest("getAccountInfo", params)
            rpcApi.getAccountInfo(rpcRequest).result
        } catch (e: EmptyDataException) {
            Timber.w("`getAccountInfo` responded with empty data, returning null")
            null
        }
    }

    override suspend fun getAccountsInfo(accounts: List<String>): List<Pair<String, AccountInfo>> {
        val requestsBatch = accounts.map {
            val params = listOf(it, RequestConfiguration(encoding = Encoding.BASE64.encoding))
            RpcRequest("getAccountInfo", params)
        }

        return try {
            rpcApi
                .getAccountsInfo(requestsBatch)
                .mapIndexed { index, response ->
                    requestsBatch[index].params!!.first() as String to response.result
                }
        } catch (e: EmptyDataException) {
            Timber.w("`getAccountsInfo` responded with empty data, returning null")
            emptyList()
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
            response.result
        } catch (e: EmptyDataException) {
            Timber.w("`getProgramAccounts` responded with empty data, returning empty list")
            emptyList()
        }
    }

    override suspend fun getTokenAccountsByOwner(owner: String): TokenAccounts {
        val programId = TokenProgram.PROGRAM_ID
        val programIdParam = HashMap<String, String>()
        programIdParam["programId"] = programId.toBase58()

        val encoding = HashMap<String, String>()
        encoding["encoding"] = "jsonParsed"

        val params = listOf(
            owner,
            programIdParam,
            encoding
        )

        val rpcRequest = RpcRequest("getTokenAccountsByOwner", params)
        return rpcApi.getTokenAccountsByOwner(rpcRequest = rpcRequest).result
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
}