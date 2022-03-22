package org.p2p.solanaj.kits.transaction

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.model.types.RpcResultObject

data class ConfirmedTransactionParsed(
    @SerializedName("blockTime")
    val blockTime: Long,

    @SerializedName("slot")
    val slot: Int,

    @SerializedName("transaction")
    val transaction: TransactionParsed?,

    @SerializedName("meta")
    val meta: Meta,
) : RpcResultObject() {
    data class AccountKeysParsed(
        @SerializedName("pubkey")
        val pubkey: String?,

        @SerializedName("signer")
        private val signer: Boolean,

        @SerializedName("writable")
        private val writable: Boolean
    )

    data class Parsed(
        @SerializedName("info")
        val info: Map<String, Any>,

        @SerializedName("type")
        val type: String?
    )

    data class InstructionParsed(
        @SerializedName("parsed")
        val parsed: Parsed?,

        @SerializedName("program")
        val program: String?,

        @SerializedName("programId")
        val programId: String?,

        @SerializedName("accounts")
        val accounts: List<String>,

        @SerializedName("data")
        val data: String?
    )

    data class MessageParsed(
        @SerializedName("accountKeys")
        val accountKeys: List<AccountKeysParsed>,

        @SerializedName("instructions")
        val instructions: List<InstructionParsed>,

        @SerializedName("recentBlockhash")
        private val recentBlockhash: String
    )

    data class TransactionParsed(
        @SerializedName("message")
        val message: MessageParsed,

        @SerializedName("signatures")
        val signatures: List<String>
    )

    data class InnerInstruction(
        @SerializedName("index")
        private val index: Int,

        @SerializedName("instructions")
        val instructions: List<InstructionParsed>
    )

    data class TokenBalance(
        @SerializedName("accountIndex")
        val accountIndex: Int,

        @SerializedName("mint")
        val mint: String?
    )

    data class Meta(
        @SerializedName("fee")
        val fee: Long,

        @SerializedName("innerInstructions")
        val innerInstructions: List<InnerInstruction>,

        @SerializedName("postTokenBalances")
        val postTokenBalances: List<TokenBalance>,

        @SerializedName("preTokenBalances")
        val preTokenBalances: List<TokenBalance>
    )
}
