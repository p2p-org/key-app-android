package org.p2p.wallet.infrastructure.network.feerelayer

import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.INVALID_TRANSACTION_ERROR_CODE
import org.p2p.wallet.infrastructure.network.data.TRANSACTION_SIMULATION_FAILED_ERROR_CODE
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorType

object ErrorTypeConverter {

    fun fromFeeRelayer(error: FeeRelayerError): ErrorCode {
        return when (error.type) {
            FeeRelayerErrorType.SLIPPAGE_LIMIT -> ErrorCode.SLIPPAGE_LIMIT
            FeeRelayerErrorType.INSUFFICIENT_FUNDS -> ErrorCode.INSUFFICIENT_FUNDS
            else -> when (error.code) {
                INVALID_TRANSACTION_ERROR_CODE -> ErrorCode.INVALID_TRANSACTION
                TRANSACTION_SIMULATION_FAILED_ERROR_CODE -> ErrorCode.TRANSACTION_SIMULATION_FAILED
                else -> ErrorCode.SERVER_ERROR
            }
        }
    }

    fun fromMoonpay(errorType: MoonpayErrorType): ErrorCode {
        // TODO: Add implementation
        return when (errorType) {
            MoonpayErrorType.BAD_REQUEST_ERROR -> ErrorCode.BAD_REQUEST
            else -> ErrorCode.SERVER_ERROR
        }
    }
}
