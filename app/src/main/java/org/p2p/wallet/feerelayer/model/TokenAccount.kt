package org.p2p.wallet.feerelayer.model

import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT

data class TokenAccount(
    val address: String,
    val mint: String
) {

    val isSOL: Boolean = WRAPPED_SOL_MINT == mint
}
