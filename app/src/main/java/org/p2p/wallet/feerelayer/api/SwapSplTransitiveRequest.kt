package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

class SwapSplTransitiveRequest(
    @SerializedName("from")
    val from: SwapSplRequest,
    @SerializedName("to")
    val to: SwapSplRequest,
    @SerializedName("transit_token_mint_pubkey")
    val transitTokenMintPubkey: String,
    @SerializedName("needs_create_transit_token_account")
    val needsCreateTransitTokenAccount: Boolean
)
