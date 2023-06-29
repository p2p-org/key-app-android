package org.p2p.wallet.history.repository.remote

import kotlinx.coroutines.withContext
import java.util.Optional
import org.p2p.core.utils.Constants
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.history.api.RpcHistoryServiceApi
import org.p2p.wallet.history.api.model.RpcHistoryTransactionResponse
import org.p2p.wallet.history.interactor.mapper.RpcHistoryTransactionConverter
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

private const val REQUEST_PARAMS_USER_ID = "user_id"
private const val REQUEST_PARAMS_LIMIT = "limit"
private const val REQUEST_PARAMS_OFFSET = "offset"
private const val REQUEST_PARAMS_SIGNATURE = "signature"
private const val REQUEST_PARAMS_NAME = "get_transactions"
private const val REQUEST_PARAMS_MINT = "mint"

class RpcHistoryRepository(
    private val historyApi: RpcHistoryServiceApi,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyServiceSignatureFieldGenerator: HistoryServiceSignatureFieldGenerator,
    private val converter: RpcHistoryTransactionConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
) : HistoryRemoteRepository {

    private val allTransactions = mutableMapOf<String, MutableList<RpcHistoryTransactionResponse>>()
    private val tokenPagingState = mutableMapOf<String, HistoryPagingState>()

    override suspend fun loadHistory(limit: Int, mintAddress: String): HistoryPagingResult =
        withContext(coroutineDispatchers.io) {
            allTransactions.clear()
            tokenPagingState.clear()
            return@withContext try {
                val response = fetchHistoryTransactions(limit, mintAddress.takeIf { it != Constants.WRAPPED_SOL_MINT })
                val domainItems = response.mapNotNull { converter.toDomain(it) }
                HistoryPagingResult.Success(domainItems)
            } catch (e: Throwable) {
                HistoryPagingResult.Error(e)
            }
        }

    override suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult =
        withContext(coroutineDispatchers.io) {
            return@withContext try {
                val response = fetchHistoryTransactions(limit, mintAddress.takeIf { it != Constants.WRAPPED_SOL_MINT })
                val domainItems = response.mapNotNull { converter.toDomain(it) }
                HistoryPagingResult.Success(domainItems)
            } catch (e: Throwable) {
                HistoryPagingResult.Error(e)
            }
        }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        return allTransactions.flatMap { it.value }
            .firstOrNull {
                id == it.signature
            }?.let {
                converter.toDomain(it)
            }
    }

    override fun getPagingState(mintAddress: String?): HistoryPagingState {
        val tokenAddress = mintAddress ?: tokenKeyProvider.publicKey
        return tokenPagingState.getOrPut(tokenAddress) { HistoryPagingState.ACTIVE }
    }

    private suspend fun fetchHistoryTransactions(
        limit: Int,
        mintAddress: String?,
    ): List<RpcHistoryTransactionResponse> = withContext(coroutineDispatchers.io) {
        val tokenAddress = mintAddress ?: tokenKeyProvider.publicKey
        val historyPagingState = getPagingState(tokenAddress)
        if (historyPagingState == HistoryPagingState.INACTIVE) {
            return@withContext findTransactionsByTokenAddress(tokenAddress)
        }

        val offset = findTransactionsByTokenAddress(tokenAddress).size.toLong()
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
            REQUEST_PARAMS_SIGNATURE to signature,
            REQUEST_PARAMS_MINT to mintAddress
        )

        val rpcRequest = RpcMapRequest(
            method = REQUEST_PARAMS_NAME,
            params = requestParams
        )
        val localTransactions = findTransactionsByTokenAddress(
            token = tokenAddress
        )
        return@withContext try {
            val result = historyApi.getTransactionHistory(rpcRequest).result.transactions

            if (result.isEmpty() || result.size < limit) {
                tokenPagingState[tokenAddress] = HistoryPagingState.INACTIVE
            }
            if (!localTransactions.containsAll(result)) {
                localTransactions.addAll(result)
            }
            allTransactions[tokenAddress] = localTransactions
            findTransactionsByTokenAddress(tokenAddress)
        } catch (e: EmptyDataException) {
            tokenPagingState[tokenAddress] = HistoryPagingState.INACTIVE
            findTransactionsByTokenAddress(tokenAddress)
        }
    }

    private fun findTransactionsByTokenAddress(token: String): MutableList<RpcHistoryTransactionResponse> {
        return allTransactions.getOrPut(token) { mutableListOf() }
    }
}
