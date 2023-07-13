package org.p2p.wallet.receive.list

import org.p2p.core.token.TokenMetadata
import org.p2p.wallet.utils.emptyString

data class TokenListData(
    val searchText: String = emptyString(),
    val result: List<TokenMetadata> = emptyList()
) {
    val size = result.size
}
