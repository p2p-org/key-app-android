package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

class RelaySwapRequest(
    @SerializedName("user_source_token_account_pubkey")
    val userSourceTokenAccountPubkey: String,
    @SerializedName("user_destination_pubkey")
    val userDestinationPubkey: String,
    @SerializedName("source_token_mint_pubkey")
    val sourceTokenMintPubkey: String,
    @SerializedName("destination_token_mint_pubkey")
    val destinationTokenMintPubkey: String,
    @SerializedName("user_authority_pubkey")
    val userAuthorityPubkey: String,
    @SerializedName("user_swap")
    val userSwap: SwapDataRequest,
    @SerializedName("fee_amount")
    val feeAmount: Long,
    @SerializedName("signatures")
    val signatures: SwapTransactionSignaturesRequest,
    @SerializedName("blockhash")
    val blockhash: String
)