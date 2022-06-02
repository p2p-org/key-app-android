package org.p2p.solanaj.kits.transaction

class CloseAccountDetails(
    signature: String,
    blockTime: Long,
    slot: Int,
    override var account: String? = null,
    val mint: String?
) : TransactionDetails(signature, blockTime, slot) {
    override val type: TransactionDetailsType = TransactionDetailsType.CLOSE_ACCOUNT
}
