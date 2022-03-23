package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.FeeAmount
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair

class FeesAndPools(
    val fee: FeeAmount,
    val poolsPair: OrcaPoolsPair
)
