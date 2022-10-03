package org.p2p.wallet.sdk.facade.mapper

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.sdk.facade.model.SolendMethodResultException

class SolendResult<SuccessType>(
    @SerializedName("success")
    val success: SuccessType? = null,
    @SerializedName("error")
    val error: SolendMethodResultException? = null
)
