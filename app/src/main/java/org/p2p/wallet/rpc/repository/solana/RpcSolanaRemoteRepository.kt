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
import org.p2p.solanaj.model.types.SignatureInformation
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository

class RpcSolanaRemoteRepository(
    private val api: RpcSolanaApi,
    private val blockHashRepository: RpcBlockhashRepository
) : RpcSolanaRepository {

    override suspend fun sendTransaction(transaction: Transaction, signer: Account): String {
        val blockHash = blockHashRepository.getRecentBlockhash().recentBlockhash
        transaction.recentBlockHash = blockHash
        transaction.sign(signer)
        val serializedTransaction = transaction.serialize()

        val base64Transaction = Base64Utils.encode(serializedTransaction)

        val params = arrayListOf<Any>().apply {
            add(base64Transaction)
            add(RpcSendTransactionConfig())
        }
        return api.sendTransaction(
            RpcRequest(
                method = "sendTransaction",
                params = params
            )
        ).result
    }

    override suspend fun getConfirmedSignaturesForAddress(
        mintLogAccount: PublicKey,
        limit: Int
    ): List<SignatureInformation> {
        val params = arrayListOf<Any>().apply {
            add(mintLogAccount.toString())
            add(ConfigObjects.ConfirmedSignFAddr2(limit))
        }
        val rawResult = api.getConfirmedSignatureForAddress(
            RpcRequest(method = "getConfirmedSignaturesForAddress2", params = params)
        ).result

        val result = arrayListOf<SignatureInformation>().apply {
            for (item in rawResult) {
                add(SignatureInformation(item))
            }
        }
        return result
    }

    override suspend fun getQueryMint(params: HashMap<String, String>): ResponseQueryTxMint =
        api.queryMint(RpcRequest2(method = "ren_queryTx", params = params)).result

    override suspend fun getQueryBlockState(): ResponseQueryBlockState =
        api.queryBlockState(RpcRequest2(method = "ren_queryBlockState", emptyMap())).result

    override suspend fun getQueryConfig(): ResponseQueryConfig =
        api.queryConfig(RpcRequest2(method = "ren_queryConfig", params = emptyMap())).result

    override suspend fun submitTx(
        hash: String,
        mintTx: ParamsSubmitMint.MintTransactionInput,
        selector: String
    ): ResponseSubmitTxMint {
        val submitMint = ParamsSubmitMint(hash, mintTx, selector)
        val params = hashMapOf<String, Any>()
        params["tx"] = submitMint
        return api.submitTx(RpcRequest2(method = "ren_submitTx", params = params)).result
    }

    override suspend fun getAccountInfo(stateKey: PublicKey): AccountInfo {
        val params = arrayListOf<Any>().apply {
            add(stateKey.toString())
            add(RpcSendTransactionConfig())
        }

        return api.getAccountInfo(RpcRequest(method = "getAccountInfo", params = params)).result
    }
}
