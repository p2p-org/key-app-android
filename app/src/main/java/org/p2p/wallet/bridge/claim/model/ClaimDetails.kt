package org.p2p.wallet.bridge.claim.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.bridge.model.BridgeAmount

@Parcelize
data class ClaimDetails(
    val willGetAmount: BridgeAmount,
    val networkFee: BridgeAmount,
    val accountCreationFee: BridgeAmount,
    val bridgeFee: BridgeAmount,
    val minAmountForFreeFee: BigDecimal,
    val claimAmount: BigDecimal
) : Parcelable
