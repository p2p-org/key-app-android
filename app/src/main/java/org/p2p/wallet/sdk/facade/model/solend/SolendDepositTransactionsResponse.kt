package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName

class SolendDepositTransactionsResponse(
    @SerializedName("transactions")
    val transactions: List<String>
)
