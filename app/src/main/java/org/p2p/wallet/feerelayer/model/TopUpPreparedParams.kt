package org.p2p.wallet.feerelayer.model

import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import java.math.BigInteger

class TopUpPreparedParams(
    val amount: BigInteger,
    val expectedFee: BigInteger,
    val poolsPair: OrcaPoolsPair
)
