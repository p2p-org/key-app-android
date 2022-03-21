package org.p2p.solanaj.kits.transaction

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

    val decimals: Int
        get() {
            // if there is no decimals, then putting SOL decimals instead
            return if (_decimals == 0) 9 else _decimals
        }

    val isSimpleTransfer: Boolean = typeStr == "transfer"

    override val type: TransactionDetailsType = TransactionDetailsType.TRANSFER

    override val info: Any = this
}