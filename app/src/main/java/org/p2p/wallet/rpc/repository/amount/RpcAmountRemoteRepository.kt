package org.p2p.wallet.rpc.repository.amount

import org.koin.ext.getFullName
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.rpc.api.RpcAmountApi
import timber.log.Timber
import java.math.BigInteger

class RpcAmountRemoteRepository(
    private val rpcApi: RpcAmountApi
) : RpcAmountRepository {

    private val lamportsPerSignatureCache = mutableMapOf<String?, BigInteger>()
    private val rentExemptionCache = mutableMapOf<Int, BigInteger>()

    override suspend fun getLamportsPerSignature(commitment: String?): BigInteger {
        val cachedLamports = lamportsPerSignatureCache[commitment]
        return if (cachedLamports != null) {
            Timber.tag(RpcAmountRemoteRepository::class.getFullName())
                .d("Getting from cache, lamportsPerSignature: $cachedLamports")

            cachedLamports
        } else {
            val params = commitment?.let {
                val config = RequestConfiguration(commitment = it)
                listOf(config)
            }
            val rpcRequest = RpcRequest("getFees", params)
            val response = rpcApi.getFees(rpcRequest).result
            val result = response.value.feeCalculator.lamportsPerSignature.toBigInteger()
            lamportsPerSignatureCache[commitment] = result
            return result
        }
    }

    override suspend fun getMinBalanceForRentExemption(dataLength: Int): BigInteger {
        val cachedBalance = rentExemptionCache[dataLength]
        return if (cachedBalance != null) {
            Timber.tag(RpcAmountRepository::class.getFullName())
                .d("Getting from cache: dataLength: $dataLength $cachedBalance")
            cachedBalance
        } else {
            val params = listOf(dataLength)
            val rpcRequest = RpcRequest("getMinimumBalanceForRentExemption", params)
            val result = rpcApi.getMinimumBalanceForRentExemption(rpcRequest).result.toBigInteger()
            rentExemptionCache[dataLength] = result
            return result
        }
            .also { Timber.i("min_balance_for_rent_exemption: $it") }
    }
}
