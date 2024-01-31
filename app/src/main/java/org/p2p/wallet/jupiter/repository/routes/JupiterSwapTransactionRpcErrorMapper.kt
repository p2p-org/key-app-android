package org.p2p.wallet.jupiter.repository.routes

import org.p2p.core.network.data.ServerException
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.wallet.jupiter.interactor.JupiterSwapTokensResult

class JupiterSwapTransactionRpcErrorMapper {
    private companion object {
        private const val JUPITER_LOW_SLIPPAGE_ERROR_CODE = 6001L
        private const val WHIRPOOLS_INVALID_TIMESTAMP_ERROR_CODE = 6022L
    }

    fun mapRpcError(blockchainError: ServerException): JupiterSwapTokensResult.Failure {
        val domainErrorType = blockchainError.domainErrorType
        return when {
            domainErrorType !is RpcTransactionError.InstructionError -> {
                blockchainError
            }
            domainErrorType.isLowSlippageError() -> {
                JupiterSwapTokensResult.Failure.LowSlippageRpcError(blockchainError)
            }
            domainErrorType.isInvalidTimestampError() -> {
                JupiterSwapTokensResult.Failure.InvalidTimestampRpcError(blockchainError)
            }
            else -> {
                blockchainError
            }
        }.let(JupiterSwapTokensResult::Failure)
    }

    private fun RpcTransactionError.InstructionError.isLowSlippageError(): Boolean =
        extractCustomErrorCodeOrNull() == JUPITER_LOW_SLIPPAGE_ERROR_CODE

    /**
     * @see [org.p2p.wallet.jupiter.interactor.JupiterSwapTokensResult.Failure.InvalidTimestampRpcError]
     */
    private fun RpcTransactionError.InstructionError.isInvalidTimestampError(): Boolean =
        extractCustomErrorCodeOrNull() == WHIRPOOLS_INVALID_TIMESTAMP_ERROR_CODE
}
