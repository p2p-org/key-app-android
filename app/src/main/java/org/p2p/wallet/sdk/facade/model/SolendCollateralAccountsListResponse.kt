package org.p2p.wallet.sdk.facade.model

import com.google.gson.annotations.SerializedName

class SolendCollateralAccountsListResponse(
    @SerializedName("accounts")
    val accounts: List<SolendCollateralAccountResponse>
)
