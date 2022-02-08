package org.p2p.wallet.send.model

import java.math.BigDecimal

class SendTotal constructor(
    val total: BigDecimal,
    val totalUsd: BigDecimal?,
    val receive: String,
    val receiveUsd: String?,
    val fee: SendFee?,
    val sourceSymbol: String
) {

    fun getTotalFee(): String =
        when (fee) {
            is SendFee.SolanaFee ->
                if (sourceSymbol == fee.feePayerSymbol) "${total + fee.fee} $sourceSymbol"
                else "$totalFormatted + ${fee.fee} ${fee.feePayerSymbol}"
            is SendFee.RenBtcFee ->
                "$totalFormatted + ${fee.fee} ${fee.feePayerSymbol}"
            else ->
                totalFormatted
        }

    val fullTotal: String
        get() = if (approxTotalUsd != null) "$totalFormatted $approxTotalUsd" else totalFormatted

    val approxTotalUsd: String? get() = totalUsd?.let { "(~$$it)" }

    val fullReceive: String
        get() = if (approxReceive != null) "$receive $approxReceive" else receive

    val approxReceive: String?
        get() = receiveUsd?.let { "(~$$it)" }

    private val totalFormatted: String
        get() = "$total $sourceSymbol"
}