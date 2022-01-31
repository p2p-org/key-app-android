package org.p2p.wallet.feerelayer.model

import java.math.BigInteger

class TopUpPreparedParams(
    val topUpFeesAndPools: FeesAndPools?,
    val topUpAmount: BigInteger?
)