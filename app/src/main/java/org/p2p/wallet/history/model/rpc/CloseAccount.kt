package org.p2p.wallet.history.model.rpc

import kotlinx.android.parcel.Parcelize
import org.p2p.core.token.TokenData
import org.p2p.wallet.transaction.model.TransactionStatus
import org.threeten.bp.ZonedDateTime

@Parcelize
data class CloseAccount(
    override val date: ZonedDateTime,
    override val signature: String,
    override val blockNumber: Int,
    override val status: TransactionStatus,
    val account: String,
    val iconUrl: String,
    val tokenSymbol: String,
) : HistoryTransaction(date) {

    fun getInfo(operationText: String): String = if (tokenSymbol.isNotBlank()) {
        "$tokenSymbol $operationText"
    } else {
        operationText
    }
}
