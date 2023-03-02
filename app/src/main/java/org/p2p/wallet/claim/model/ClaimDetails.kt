package org.p2p.wallet.claim.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClaimDetails(
    val willGet: ClaimFee,
    val networkFee: ClaimFee,
    val accountCreationFee: ClaimFee,
    val bridgeFee: ClaimFee,
) : Parcelable
