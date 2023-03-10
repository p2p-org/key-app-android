package org.p2p.wallet.history.api.model
import com.google.gson.annotations.SerializedName

enum class RpcHistoryFeeTypeResponse {
    @SerializedName("transaction")
    TRANSACTION,
    @SerializedName("create_account")
    CREATE_ACCOUNT
}
