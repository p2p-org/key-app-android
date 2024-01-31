package org.p2p.wallet.feerelayer.model

import java.math.BigInteger
import org.p2p.solanaj.core.FeeAmount

sealed interface FeePoolsState {
    data class Calculated(val feeInSpl: FeeAmount) : FeePoolsState
    data class Failed(val feeInSOL: FeeAmount) : FeePoolsState

    val total: BigInteger
        get() = when (this) {
            is Calculated -> feeInSpl.totalFeeLamports
            is Failed -> feeInSOL.totalFeeLamports
        }
}
