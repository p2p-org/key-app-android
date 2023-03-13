package org.p2p.wallet.claim.api.response

import com.google.gson.annotations.SerializedName

class FeesResponse(
    @SerializedName("gas")
    val gasFee: FeeResponse? = null,
    @SerializedName("arbiter")
    val arbiterFee: FeeResponse? = null,
    @SerializedName("create_account")
    val createAccountFee: FeeResponse? = null
)
