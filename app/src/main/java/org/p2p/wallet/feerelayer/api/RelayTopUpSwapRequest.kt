package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

class RelayTopUpSwapRequest(
    @SerializedName("user_source_token_account_pubkey")
    val userSourceTokenAccountPubkey: String,
    @SerializedName("source_token_mint_pubkey")
    val sourceTokenMintPubkey: String,
    @SerializedName("user_authority_pubkey")
    val userAuthorityPubkey: String,
    @SerializedName("top_up_swap")
    val topUpSwap: SwapDataRequest,
    @SerializedName("fee_amount")
    val feeAmount: Long,
    @SerializedName("signatures")
    val signatures: SwapTransactionSignaturesRequest,
    @SerializedName("blockhash")
    val blockhash: String
)
