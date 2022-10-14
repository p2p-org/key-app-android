package org.p2p.wallet.rpc.repository.solana

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcRequest2
import org.p2p.solanaj.model.types.RpcSendTransactionConfig
import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.rpc.model.RecentPerformanceSample
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository

class RpcSolanaRemoteRepository(
    private val api: RpcSolanaApi,
    private val blockHashRepository: RpcBlockhashRepository,
    private val environmentManager: NetworkEnvironmentManager
) : RpcSolanaRepository {

    override suspend fun sendTransaction(transaction: Transaction, signer: Account): String {
        val blockHash = blockHashRepository.getRecentBlockhash().recentBlockhash
        transaction.recentBlockHash = blockHash
        transaction.sign(signer)

        val serializedTransaction = transaction.serialize()
        val base64Transaction = Base64Utils.encode(serializedTransaction)
        val params = arrayListOf(base64Transaction, RpcSendTransactionConfig())

        return api.sendTransaction(RpcRequest(method = "sendTransaction", params = params)).result
    }

    override suspend fun getRecentPerformanceSamples(exampleCount: Int): List<RecentPerformanceSample> {
        val params = arrayListOf(exampleCount)

        val rpcRequest = RpcRequest(method = "getRecentPerformanceSamples", params = params)
        val response = api.getRecentPerformanceSamples(rpcRequest)
        return response.result.map {
            RecentPerformanceSample(
                numSlots = it.numSlots,
                numTransactions = it.numTransactions,
                samplePeriodSecs = it.samplePeriodSecs,
                slot = it.slot,
            )
        }
    }

    override suspend fun getConfirmedSignaturesForAddress(
        mintLogAccount: PublicKey,
        limit: Int
    ): List<SignatureInformationResponse> {
        val params = arrayListOf(mintLogAccount.toString(), ConfigObjects.ConfirmedSignFAddr2(limit))
        val rawResult = api.getConfirmedSignatureForAddress(
            RpcRequest(method = "getSignaturesForAddress", params = params)
        ).result

        return rawResult.map { SignatureInformationResponse(it) }
    }

    override suspend fun getQueryMint(txHash: String): ResponseQueryTxMint {
        val baseUrl = environmentManager.loadRpcEnvironment().lightNode
        val params = hashMapOf<String, Any>("txHash" to txHash)
        return api.queryMint(url = baseUrl, rpcRequest = RpcRequest2(method = "ren_queryTx", params = params)).result
    }

    override suspend fun getQueryBlockState(): ResponseQueryBlockState {
        val baseUrl = environmentManager.loadRpcEnvironment().lightNode

        val rpcRequest = RpcRequest2(method = "ren_queryBlockState", params = emptyMap())

        return api.queryBlockState(url = baseUrl, rpcRequest = rpcRequest).result
    }

    override suspend fun getQueryConfig(): ResponseQueryConfig {
        val baseUrl = environmentManager.loadRpcEnvironment().lightNode
        val rpcRequest = RpcRequest2(method = "ren_queryConfig", params = emptyMap())

        return api.queryConfig(
            url = baseUrl,
            rpcReuest = rpcRequest
        ).result
    }

    override suspend fun submitTx(
        hash: String,
        mintTx: ParamsSubmitMint.MintTransactionInput,
        selector: String
    ): ResponseSubmitTxMint {

        val baseUrl = environmentManager.loadRpcEnvironment().lightNode
        val submitMint = ParamsSubmitMint(hash, mintTx, selector)
        val params = hashMapOf<String, Any>("tx" to submitMint)
        return api.submitTx(
            url = baseUrl,
            rpcRequest = RpcRequest2(method = "ren_submitTx", params = params)
        ).result
    }

    override suspend fun getAccountInfo(stateKey: PublicKey): AccountInfo {
        val params = arrayListOf(stateKey.toString(), RpcSendTransactionConfig())
        return api.getAccountInfo(RpcRequest(method = "getAccountInfo", params = params)).result
    }
}
