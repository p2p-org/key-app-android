package org.p2p.wallet.feerelayer.model

import org.p2p.wallet.feerelayer.api.SwapDataRequest
import org.p2p.wallet.feerelayer.api.SwapSplRequest
import org.p2p.wallet.feerelayer.api.SwapSplTransitiveRequest

object SwapDataConverter {

    fun toNetwork(data: SwapData): SwapDataRequest = when (data) {
        is SwapData.Direct -> SwapDataRequest(
            spl = toSpl(data),
            splTransitive = null
        )
        is SwapData.SplTransitive -> SwapDataRequest(
            spl = null,
            splTransitive = SwapSplTransitiveRequest(
                from = toSpl(data.from),
                to = toSpl(data.to),
                transitTokenMintPubkey = data.transitTokenMintPubkey,
                needsCreateTransitTokenAccount = data.needsCreateTransitTokenAccount
            ),
        )
    }

    private fun toSpl(data: SwapData.Direct): SwapSplRequest =
        SwapSplRequest(
            programId = data.programId,
            accountPubkey = data.accountPubkey,
            authorityPubkey = data.authorityPubkey,
            transferAuthorityPubkey = data.transferAuthorityPubkey,
            sourcePubkey = data.sourcePubkey,
            destinationPubkey = data.destinationPubkey,
            poolTokenMintPubkey = data.poolTokenMintPubkey,
            poolFeeAccountPubkey = data.poolFeeAccountPubkey,
            amountIn = data.amountIn.toLong(),
            minimumAmountOut = data.minimumAmountOut.toLong(),
        )
}
