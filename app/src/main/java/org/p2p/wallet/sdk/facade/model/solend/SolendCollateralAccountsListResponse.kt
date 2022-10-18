package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName

class SolendCollateralAccountsListResponse(
    @SerializedName("accounts")
    val accounts: List<SolendCollateralAccountResponse>
)
