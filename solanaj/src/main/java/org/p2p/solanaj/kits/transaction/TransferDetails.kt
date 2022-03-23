package org.p2p.solanaj.kits.transaction

private const val SOL_DECIMALS = 9

class TransferDetails(
    signature: String,
    blockTime: Long,
    slot: Int,
    val typeStr: String?,
    val fee: Long,
    val source: String?,
    val destination: String?,
    val authority: String?,
    val mint: String?,
    val amount: String?,
    private val _decimals: Int
) : TransactionDetails(signature, blockTime, slot) {

    // if there is no decimals, then putting SOL decimals instead
    val decimals: Int
        get() = if (_decimals == 0) SOL_DECIMALS else _decimals

    val isSimpleTransfer: Boolean = typeStr == "transfer"

    override val type: TransactionDetailsType = TransactionDetailsType.TRANSFER
}
