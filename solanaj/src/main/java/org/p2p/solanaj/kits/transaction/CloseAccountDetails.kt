package org.p2p.solanaj.kits.transaction

class CloseAccountDetails(
    signature: String,
    blockTime: Long,
    slot: Int,
    val account: String?,
    val mint: String?
) : TransactionDetails(signature, blockTime, slot) {
    override val type: TransactionDetailsType = TransactionDetailsType.CLOSE_ACCOUNT
}
