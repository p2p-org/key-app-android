package org.p2p.solanaj.kits.transaction

private const val SOL_DECIMALS = 9

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

    val mint: String = "CDJWUqTcYTVAKXAVXoQZFes5JUFc7owSeq7eMQcDSbo5"

    // if there is no decimals, then putting SOL decimals instead
    val decimals: Int
        get() = if (_decimals == 0) SOL_DECIMALS else _decimals

    override val type: TransactionDetailsType = TransactionDetailsType.TRANSFER
}
