package org.p2p.wallet.home.model

data class PopularToken(
    val tokenSymbol: String,
    val actionType: ActionType
)

enum class ActionType {
    Buy,
    Receive
}
