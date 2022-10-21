package org.p2p.wallet.rpc.repository.solana

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcSendTransactionConfig
import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.rpc.model.RecentPerformanceSample
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository

class RpcSolanaRemoteRepository(
    private val api: RpcSolanaApi,
    private val blockHashRepository: RpcBlockhashRepository,
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
                numberOfSlots = it.numSlots,
                numberOfTransactions = it.numTransactions,
                samplePeriodInSeconds = it.samplePeriodSecs,
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

    override suspend fun getAccountInfo(stateKey: PublicKey): AccountInfo {
        val params = arrayListOf(stateKey.toString(), RpcSendTransactionConfig())
        return api.getAccountInfo(RpcRequest(method = "getAccountInfo", params = params)).result
    }
}
