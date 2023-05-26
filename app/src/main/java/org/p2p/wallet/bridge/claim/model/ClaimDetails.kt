package org.p2p.wallet.bridge.claim.model

import android.os.Parcelable
import java.math.BigDecimal
import org.threeten.bp.ZonedDateTime
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.bridge.model.BridgeAmount

@Parcelize
data class ClaimDetails(
    val isFree: Boolean,
    val willGetAmount: BridgeAmount,
    val networkFee: BridgeAmount,
    val accountCreationFee: BridgeAmount,
    val bridgeFee: BridgeAmount,
    val totalAmount: BridgeAmount,
    val minAmountForFreeFee: BigDecimal,
    val transactionDate: ZonedDateTime,
) : Parcelable
