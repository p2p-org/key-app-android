package org.p2p.solanaj.kits.transaction

class CreateAccountDetails(
    override val signature: String,
    override val slot: Int,
    blockTime: Long,
    val fee: Long
) : TransactionDetails(signature, blockTime, slot) {

    override val info: Any
        get() = this

    override val type: TransactionDetailsType
        get() = TransactionDetailsType.CREATE_ACCOUNT
}