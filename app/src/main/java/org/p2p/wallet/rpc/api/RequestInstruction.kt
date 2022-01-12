package org.p2p.wallet.rpc.api

import com.google.gson.annotations.SerializedName

data class RequestInstruction(
    @SerializedName("program_id")
    val programIdIndex: Int,
    @SerializedName("accounts")
    val accounts: List<RequestAccountMeta>,
    @SerializedName("data")
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestInstruction

        if (programIdIndex != other.programIdIndex) return false
        if (accounts != other.accounts) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = programIdIndex
        result = 31 * result + accounts.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}