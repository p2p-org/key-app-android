package org.p2p.wallet.rpc.repository.solana

import com.google.gson.Gson
import com.google.gson.JsonElement
import timber.log.Timber
import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.Base64Utils
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.EpochInfo
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcSendTransactionConfig
import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.rpc.TransactionSimulationResult
import org.p2p.solanaj.rpc.model.RecentPerformanceSample
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository

private const val TAG = "RpcSolanaRemoteRepository"

class RpcSolanaRemoteRepository(
    private val api: RpcSolanaApi,
    private val blockHashRepository: RpcBlockhashRepository,
    private val gson: Gson
) : RpcSolanaRepository {

    private var epochInfoCache: EpochInfo? = null

    override suspend fun sendTransaction(
        transaction: Transaction,
        signer: Account,
        encoding: Encoding
    ): String {
        val blockHash = blockHashRepository.getRecentBlockhash().recentBlockhash
        transaction.setRecentBlockhash(blockHash)
        transaction.sign(signer)

        val serializedTransaction = transaction.serialize()
        val base64Transaction = Base64Utils.encode(serializedTransaction)
        val params = arrayListOf(base64Transaction, RpcSendTransactionConfig(encoding))

        return api.sendTransaction(RpcRequest(method = "sendTransaction", params = params)).result
    }

    override suspend fun sendTransaction(serializedTransaction: String, encoding: Encoding): String {
        val params = arrayListOf(serializedTransaction, RpcSendTransactionConfig(encoding))
        return api.sendTransaction(RpcRequest(method = "sendTransaction", params = params)).result
    }

    /**
     * @return is simulation success
     */
    override suspend fun simulateTransaction(
        serializedTransaction: String,
        encoding: Encoding
    ): TransactionSimulationResult {
        return try {
            val params = listOf(
                serializedTransaction,
                RpcSendTransactionConfig(encoding)
            )
            val request = RpcRequest(method = "simulateTransaction", params = params)

            val response = api.simulateTransaction(request).result

            val simulationError = response.value.error
            val transactionError: RpcTransactionError? = simulationError.let(::parseTransactionError)
            TransactionSimulationResult(
                isSimulationSuccess = simulationError.isJsonNull,
                errorIfSimulationFailed = transactionError?.toString() ?: simulationError.toString()
            )
        } catch (error: Throwable) {
            Timber.tag(TAG).i(error, "Simulation failed")
            TransactionSimulationResult(
                isSimulationSuccess = false,
                errorIfSimulationFailed = error.toString()
            )
        }
    }

    private fun parseTransactionError(errorObject: JsonElement): RpcTransactionError? {
        if (errorObject.isJsonNull) {
            return null
        }
        return kotlin.runCatching {
            gson.fromJson(errorObject, RpcTransactionError::class.java)
        }
            .getOrNull()
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

    /**
     * @return confirmed signature
     */
    override suspend fun sendSerializedTransaction(serializedTransaction: Base64String, encoding: Encoding): String {
        val params = listOf(serializedTransaction, RpcSendTransactionConfig(encoding))
        return api.sendTransaction(
            RpcRequest(method = "sendTransaction", params = params)
        ).result
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
        val encoding = Encoding.BASE64
        val params = arrayListOf(stateKey.toString(), RpcSendTransactionConfig(encoding))
        return api.getAccountInfo(RpcRequest(method = "getAccountInfo", params = params)).result
    }

    override suspend fun getEpochInfo(useCache: Boolean): EpochInfo {
        if (useCache && epochInfoCache != null) {
            return epochInfoCache!!
        }

        val request = RpcRequest(method = "getEpochInfo", params = null)
        return api.getEpochInfo(request).result.also {
            epochInfoCache = it
        }
    }
}
