package org.p2p.wallet.infrastructure.network.feerelayer

import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorType

object ErrorTypeConverter {

    fun fromFeeRelayer(errorType: FeeRelayerErrorType): ErrorCode {
        // TODO: Add implementation
        return ErrorCode.SERVER_ERROR
    }

    fun fromMoonpay(errorType: MoonpayErrorType): ErrorCode {
        // TODO: Add implementation
        return when (errorType) {
            MoonpayErrorType.BAD_REQUEST_ERROR -> ErrorCode.BAD_REQUEST
            else -> ErrorCode.SERVER_ERROR
        }
    }
}
