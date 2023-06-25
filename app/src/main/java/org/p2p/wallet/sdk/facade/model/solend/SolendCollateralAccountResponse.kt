package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

class SolendCollateralAccountResponse(
    @SerializedName("address")
    val accountAddress: Base58String,
    @SerializedName("mint")
    val accountMint: Base58String
)
