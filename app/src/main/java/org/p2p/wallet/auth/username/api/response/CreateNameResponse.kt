package org.p2p.wallet.auth.username.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.utils.crypto.Base64String

data class CreateNameResponse(
    @SerializedName("transaction")
    val serializedSignedCreateNameTransaction: Base64String
)
