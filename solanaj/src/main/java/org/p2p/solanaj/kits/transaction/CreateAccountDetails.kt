package org.p2p.solanaj.kits.transaction

class CreateAccountDetails(
    signature: String,
    slot: Int,
    blockTime: Long,
    val fee: Long,
    val mint: String?,
    override var account: String? = null
) : TransactionDetails(signature, blockTime, slot) {
    override val type = TransactionDetailsType.CREATE_ACCOUNT
}
