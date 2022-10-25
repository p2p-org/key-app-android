package org.p2p.wallet.feerelayer.model

import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT

class TokenAccount(
    val address: String,
    val mint: String
) {

    val isSOL: Boolean = WRAPPED_SOL_MINT == mint
}
