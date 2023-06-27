package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaUserWalletsResponse(
    @SerializedName("wallets")
    val wallets: List<StrigaUserWalletDetailsResponse>
)
