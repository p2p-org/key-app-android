package org.p2p.solanaj.kits

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.model.types.RpcResultObject

data class MultipleAccountsInfo(
    @SerializedName("value")
    var accountsInfoParsed: List<AccountInfoParsed>
) : RpcResultObject()
