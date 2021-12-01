package org.p2p.wallet.infrastructure.network.feerelayer

import org.p2p.wallet.infrastructure.network.ErrorCode

object ErrorTypeConverter {

    fun fromFeeRelayer(errorType: FeeRelayerErrorType): ErrorCode {
        // TODO: Add implementation
        return ErrorCode.SERVER_ERROR
    }
}