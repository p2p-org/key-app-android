package org.p2p.solanaj.kits.transaction

class BurnOrMintDetails(
    signature: String,
    blockTime: Long,
    slot: Int,
    val fee: Long,
    val account: String?,
    val authority: String?,
    val uiAmount: String?,
    private val _decimals: Int
) : TransactionDetails(signature, blockTime, slot) {

    val mint: String
        get() = "CDJWUqTcYTVAKXAVXoQZFes5JUFc7owSeq7eMQcDSbo5"

    val decimals: Int
        get() {
            // if there is no decimals, then putting SOL decimals instead
            return if (_decimals == 0) 9 else _decimals
        }

    override val type: TransactionDetailsType = TransactionDetailsType.TRANSFER
    override val info: Any = this
}