package org.p2p.ethereumkit.internal.api.jsonrpc

import com.google.gson.annotations.SerializedName
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.GasPrice
import java.math.BigInteger

class EstimateGasJsonRpc(
    @Transient val from: EthAddress,
    @Transient val to: EthAddress?,
    @Transient val amount: BigInteger?,
    @Transient val gasLimit: Long?,
    @Transient val gasPrice: GasPrice,
    @Transient val data: ByteArray?
) : LongJsonRpc(
        method = "eth_estimateGas",
        params = listOf(estimateGasParams(from, to, amount, gasLimit, gasPrice, data))
) {

    companion object {
        private fun estimateGasParams(from: EthAddress, to: EthAddress?, amount: BigInteger?, gasLimit: Long?, gasPrice: GasPrice, data: ByteArray?): EstimateGasParams {
            return when (gasPrice) {
                is GasPrice.Eip1559 -> {
                    EstimateGasParams.Eip1559(from, to, amount, gasLimit, gasPrice.maxFeePerGas, gasPrice.maxPriorityFeePerGas, data)
                }
                is GasPrice.Legacy -> {
                    EstimateGasParams.Legacy(from, to, amount, gasLimit, gasPrice.legacyGasPrice, data)
                }
            }
        }
    }

    private sealed class EstimateGasParams {
        data class Legacy(
            val from: EthAddress,
            val to: EthAddress?,
            @SerializedName("value")
                val amount: BigInteger?,
            @SerializedName("gas")
                val gasLimit: Long?,
            val gasPrice: Long?,
            val data: ByteArray?
        ) : EstimateGasParams()

        data class Eip1559(
            val from: EthAddress,
            val to: EthAddress?,
            @SerializedName("value")
                val amount: BigInteger?,
            @SerializedName("gas")
                val gasLimit: Long?,
            val maxFeePerGas: Long,
            val maxPriorityFeePerGas: Long,
            val data: ByteArray?
        ) : EstimateGasParams()
    }
}
