package org.p2p.wallet.home.model

sealed interface EmptyHomeItem {
    data class EmptyHomeTitleItem(
        val title: String
    ) : EmptyHomeItem

    data class EmptyHomePopularTokensItem(
        val tokens: List<Token>
    ) : EmptyHomeItem

    // special for AdapterDelegates, i couldn't find a way to create a AdapterDelegate from List<Token>
    data class EmptyHomePopularOneTokenItem(
        val token: Token
    ): EmptyHomeItem
}
