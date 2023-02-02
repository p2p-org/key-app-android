package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

class RpcHistoryInfoResponse(
    @SerializedName("counterparty")
    val counterPartyResponse: RpcHistoryCounterPartyResponse,
    @SerializedName("tokens")
    val tokensResponse: RpcHistoryTokensResponse,
    @SerializedName("swap_program")
    val swapProgram: String?,
    @SerializedName("vote_account")
    val voteAccount: String?,
    @SerializedName("fee_info")
    val feeInfoResponse: RpcHistoryFeeInfoResponse,
    @SerializedName("error_info")
    val errorInfo: String?
)
