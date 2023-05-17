package org.p2p.wallet.alarmlogger.utils

import java.net.UnknownHostException
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.feerelayer.model.FeeRelayerException
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.data.SimulationException
import org.p2p.wallet.utils.emptyString

fun Throwable.getSimulationError(): String = when (this) {
    is SimulationException -> "Simulation error: ${getDirectMessage().orEmpty()}"
    else -> emptyString()
}

fun Throwable.getFeeRelayerError(): String = when (this) {
    is FeeRelayerException -> "FeeRelayer error: ${getDirectMessage().orEmpty()}"
    else -> emptyString()
}

fun Throwable.getBlockchainError(): String = when (this) {
    is ServerException -> "Blockchain error: ${getDirectMessage().orEmpty()}"
    is UnknownHostException -> "Internet error ${message ?: localizedMessage}"
    is BridgeResult.Error -> "Bridge error: ${this.javaClass.simpleName}. ${message ?: localizedMessage}"
    is FeeRelayerException -> emptyString()
    is SimulationException -> emptyString()
    else -> "Unknown error: ${message ?: localizedMessage}"
}
