package org.p2p.wallet.alarmlogger.model

import java.net.UnknownHostException
import org.p2p.core.network.data.ServerException
import org.p2p.core.network.data.SimulationException
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.feerelayer.model.FeeRelayerException
import org.p2p.wallet.send.model.SendTransactionFailed

class AlarmThrowableFormatter {
    private fun Throwable.unwrap(): Throwable = if (this is SendTransactionFailed) cause else this

    fun formatFeeRelayerError(error: Throwable): String? = error.unwrap().run {
        return when (this) {
            is FeeRelayerException -> "FeeRelayer error: ${getDirectMessage().orEmpty()}"
            else -> null
        }
    }

    fun formatSimulationError(error: Throwable): String? = error.unwrap().run {
        return when (this) {
            is SimulationException -> "Simulation error: ${getDirectMessage().orEmpty()}"
            else -> null
        }
    }

    fun formatBlockchainError(error: Throwable): String? = error.unwrap().run {
        return when (this) {
            is FeeRelayerException -> null
            is SimulationException -> null

            is ServerException -> "Blockchain error: ${getDirectMessage().orEmpty()}"
            is UnknownHostException -> "Internet error ${message ?: localizedMessage}"
            is BridgeResult.Error -> "Bridge error: ${this.javaClass.simpleName}"
            else -> "Unknown error: ${message ?: localizedMessage}"
        }
    }
}
