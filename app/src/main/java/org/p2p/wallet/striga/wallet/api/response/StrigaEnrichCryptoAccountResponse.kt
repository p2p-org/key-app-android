package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaEnrichCryptoAccountResponse(
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("blockchainDepositAddress")
    val depositAddress: String,
    @SerializedName("blockchainNetwork")
    val network: StrigaBlockchainNetworkResponse,
)
