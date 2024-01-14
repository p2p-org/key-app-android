package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.kits.AccountInfoParsed

data class TokenAccounts(
    @SerializedName("value")
    val accounts: List<Account>
)

data class Account(
    @SerializedName("account")
    val account: AccountInfoParsed,

    @SerializedName("pubkey")
    val pubkey: String
) {
    val tokenMintAddress: String
        get() = account.data.parsed.info.mint
}
