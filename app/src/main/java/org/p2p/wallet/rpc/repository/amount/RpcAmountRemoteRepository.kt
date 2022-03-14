package org.p2p.wallet.rpc.repository.amount

import org.koin.ext.getFullName
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.rpc.repository.RpcRepository
import timber.log.Timber
import java.math.BigInteger

class RpcAmountRemoteRepository(
    private val rpcRepository: RpcRepository
) : RpcAmountRepository {

    private var lamportsPerSignature: BigInteger? = null

    override suspend fun getFees(commitment: String?): BigInteger =
        if (lamportsPerSignature != null) {
            Timber
                .tag(RpcAmountRemoteRepository::class.getFullName())
                .d("Getting from cache, lamportsPerSignature: $lamportsPerSignature")
            lamportsPerSignature!!
        } else {
            rpcRepository.getFees(commitment = null).also { lamportsPerSignature = it }
        }

    override suspend fun getFees(commitment: String?): BigInteger {
        val params = commitment?.let {
            val config = RequestConfiguration(commitment = it)
            listOf<Any>(config)
        }

        val rpcRequest = RpcRequest("getFees", params)

        val response = rpcApi.getFees(rpcRequest).result
        return BigInteger.valueOf(response.value.feeCalculator.lamportsPerSignature)
    }

    private val rentExemptionCache = mutableMapOf<Int, BigInteger>()

    override suspend fun getMinimumBalanceForRentExemption(dataLength: Int, useCache: Boolean): BigInteger {
        val cachedValue = rentExemptionCache[dataLength]
        return if (useCache && cachedValue != null) {
            Timber
                .tag(RpcAmountRepository::class.getFullName())
                .d("Getting from cache: dataLength: $dataLength $cachedValue")
            cachedValue
        } else {
            rpcRepository
                .getMinimumBalanceForRentExemption(dataLength)
                .toBigInteger()
                .also { rentExemptionCache[dataLength] = it }
        }
    }
}