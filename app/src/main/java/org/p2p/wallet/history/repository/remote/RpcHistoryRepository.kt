package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.history.api.RpcHistoryServiceApi
import org.p2p.wallet.history.api.model.RpcHistoryTransactionResponse
import org.p2p.wallet.history.interactor.mapper.RpcHistoryTransactionConverter
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import java.util.Optional

private const val REQUEST_PARAMS_USER_ID = "user_id"
private const val REQUEST_PARAMS_LIMIT = "limit"
private const val REQUEST_PARAMS_OFFSET = "offset"
private const val REQUEST_PARAMS_SIGNATURE = "signature"
private const val REQUEST_PARAMS_NAME = "get_transactions"

private const val TAG = "RpcHistoryRepository"

class RpcHistoryRepository(
    private val historyApi: RpcHistoryServiceApi,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyServiceSignatureFieldGenerator: HistoryServiceSignatureFieldGenerator,
    private val converter: RpcHistoryTransactionConverter
) : HistoryRemoteRepository {

    private val allTransactions = mutableMapOf<String, MutableList<RpcHistoryTransactionResponse>>()
    private var historyPagingState = HistoryPagingState.ACTIVE

    override suspend fun loadHistory(limit: Int, mintAddress: String?): HistoryPagingResult {
        allTransactions.clear()
        return try {
            val response = fetchHistoryTransactions(limit, mintAddress)
            val domainItems = response.map { converter.toDomain(it) }
            HistoryPagingResult.Success(domainItems)
        } catch (e: Throwable) {
            HistoryPagingResult.Error(e)
        }
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String?): HistoryPagingResult {
        return try {
            val response = fetchHistoryTransactions(limit, mintAddress)
            val domainItems = response.map { converter.toDomain(it) }
            HistoryPagingResult.Success(domainItems)
        } catch (e: Throwable) {
            HistoryPagingResult.Error(e)
        }
    }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        return allTransactions.flatMap { it.value }.firstOrNull {
            id == it.signature
        }?.let {
            converter.toDomain(it)
        }
    }

    override fun getPagingState(): HistoryPagingState {
        return historyPagingState
    }

    private suspend fun fetchHistoryTransactions(
        limit: Int,
        mintAddress: String?
    ): List<RpcHistoryTransactionResponse> {
        val tokenAddress = mintAddress ?: tokenKeyProvider.publicKey

        if (historyPagingState == HistoryPagingState.INACTIVE) {
            return findTransactionsByToken(tokenAddress)
        }

        val offset = findTransactionsByToken(tokenAddress).size.toLong()
        val signature = historyServiceSignatureFieldGenerator.generateSignature(
            pubKey = tokenKeyProvider.publicKey,
            privateKey = tokenKeyProvider.keyPair,
            offset = offset,
            limit = limit.toLong(),
            mint = Optional.ofNullable(mintAddress)
        )
        val requestParams = mapOf(
            REQUEST_PARAMS_USER_ID to tokenKeyProvider.publicKey,
            REQUEST_PARAMS_LIMIT to limit,
            REQUEST_PARAMS_OFFSET to offset,
            REQUEST_PARAMS_SIGNATURE to signature
        )

        val rpcRequest = RpcMapRequest(
            method = REQUEST_PARAMS_NAME,
            params = requestParams
        )
        val localTransactions = findTransactionsByToken(
            token = tokenAddress
        )
        return try {
            val result = historyApi.getTransactionHistory(rpcRequest).result
            if (result.size < limit) {
                historyPagingState = HistoryPagingState.INACTIVE
            }
            if (!localTransactions.containsAll(result)) {
                localTransactions.addAll(result)
            }
            allTransactions[tokenAddress] = localTransactions
            findTransactionsByToken(tokenAddress)
        } catch (e: EmptyDataException) {
            historyPagingState = HistoryPagingState.INACTIVE
            findTransactionsByToken(tokenAddress)
        }
    }

    private fun findTransactionsByToken(token: String): MutableList<RpcHistoryTransactionResponse> {
        return allTransactions.getOrPut(token) { mutableListOf() }
    }
}
