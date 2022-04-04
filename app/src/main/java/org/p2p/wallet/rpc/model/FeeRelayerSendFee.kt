package org.p2p.wallet.rpc.model

import org.p2p.wallet.utils.AmountInLamports

data class FeeRelayerSendFee(
    val solFee: AmountInLamports,
    val payingTokenFee: AmountInLamports?
)
