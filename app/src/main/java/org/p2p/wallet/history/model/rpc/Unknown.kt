package org.p2p.wallet.history.model.rpc

import kotlinx.android.parcel.Parcelize
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.utils.CUT_4_SYMBOLS
import org.threeten.bp.ZonedDateTime

private const val ADDRESS_SYMBOL_COUNT = 10

@Parcelize
data class Unknown(
    override val signature: String,
    override val date: ZonedDateTime,
    override val blockNumber: Int,
    override val status: TransactionStatus
) : HistoryTransaction(date)

fun cutAddress(address: String): String {
    if (address.length < ADDRESS_SYMBOL_COUNT) {
        return address
    }

    val firstSix = address.take(CUT_4_SYMBOLS)
    val lastFour = address.takeLast(CUT_4_SYMBOLS)
    return "$firstSix...$lastFour"
}
