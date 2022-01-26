package org.p2p.wallet.rpc.model

import java.math.BigInteger

data class FeeRelayerSendFee(
    val accountCreationFee: BigInteger,
    val networkFee: BigInteger,
    val userRelayAccountCreation: BigInteger
)