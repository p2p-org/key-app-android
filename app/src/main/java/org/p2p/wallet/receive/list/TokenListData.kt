package org.p2p.wallet.receive.list

import org.p2p.wallet.user.model.TokenData
import org.p2p.wallet.utils.emptyString

data class TokenListData(
    val searchText: String = emptyString(),
    val result: List<TokenData> = emptyList()
) {
    val size = result.size
}
