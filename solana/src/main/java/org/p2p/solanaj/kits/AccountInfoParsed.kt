package org.p2p.solanaj.kits

import com.google.gson.annotations.SerializedName

data class AccountInfoParsed(
    @SerializedName("data")
    val data: Data,

    @SerializedName("executable")
    val isExecutable: Boolean,

    @SerializedName("lamports")
    val lamports: Long,

    @SerializedName("owner")
    val owner: String,

    @SerializedName("rentEpoch")
    val rentEpoch: Long
) {

    var address: String? = null

    data class RootValue(
        @SerializedName("value")
        val value: AccountInfoParsed
    )
}

data class Info(
    @SerializedName("decimals")
    val decimals: Long,

    @SerializedName("freezeAuthority")
    val freezeAuthority: Any,

    @SerializedName("isInitialized")
    val isIsInitialized: Boolean,

    @SerializedName("mintAuthority")
    val mintAuthority: String,

    @SerializedName("supply")
    val supply: String,

    @SerializedName("mint")
    val mint: String,

    @SerializedName("tokenAmount")
    val tokenAmount: TokenAmount,

    // !! upd: Jan 15, 2024 !! //

    @SerializedName("isNative")
    val isNative: Boolean,

    @SerializedName("owner")
    val owner: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("extensions")
    val extensions: List<AccountInfoTokenExtension>? = emptyList()
)

data class Parsed(
    @SerializedName("info")
    val info: Info,

    @SerializedName("type")
    val type: String
)

data class Data(
    @SerializedName("parsed")
    val parsed: Parsed,

    @SerializedName("program")
    val program: String,

    @SerializedName("space")
    val space: Long = 0
)

data class TokenAmount(
    @SerializedName("amount")
    val amount: String,

    @SerializedName("decimals")
    val decimals: Long,

    @SerializedName("uiAmount")
    val uiAmount: Float,

    @SerializedName("uiAmountString")
    val uiAmountString: String
)
