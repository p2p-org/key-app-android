package org.p2p.wallet.receive.list

import org.p2p.wallet.user.model.TokenData

data class TokenListData(
    val searchText: String = "",
    val result: List<TokenData> = emptyList()
) {
    fun getSize() = result.size
}