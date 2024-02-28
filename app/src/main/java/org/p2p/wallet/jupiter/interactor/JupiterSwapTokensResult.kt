package org.p2p.wallet.jupiter.interactor

import org.p2p.core.network.data.ServerException

sealed interface JupiterSwapTokensResult {
    data class Success(val signature: String) : JupiterSwapTokensResult
    data class Failure(override val cause: Throwable) : Throwable(), JupiterSwapTokensResult {
        class LowSlippageRpcError(override val cause: ServerException) : Throwable(cause.message)

        /**
         *
         * https://github.com/orca-so/whirlpools/blob/e06505fb1e41508295eca116ca676c8f498398d2/programs/whirlpool/src/manager/whirlpool_manager.rs#L13
         * happens randomly error occurs when sending transaction,
         * but it's not our fault, it depends on the node state
         * https://discord.com/channels/767761912167006292/1106227799448101004
         */
        class InvalidTimestampRpcError(override val cause: ServerException) : Throwable(cause.message)
    }
}
