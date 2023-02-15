package org.p2p.wallet.history.model.rpc

import kotlinx.android.parcel.Parcelize
import org.p2p.wallet.transaction.model.TransactionStatus
import org.threeten.bp.ZonedDateTime
import java.math.BigInteger

@Parcelize
data class CreateAccount(
    override val date: ZonedDateTime,
    override val signature: String,
    override val blockNumber: Int,
    override val status: TransactionStatus,
    val iconUrl: String,
    val fee: BigInteger,
    val tokenSymbol: String,
) : HistoryTransaction(date) {

    fun getTokenIconUrl(): String? = iconUrl

    fun getInfo(operationText: String): String = if (tokenSymbol.isNotBlank()) {
        "$tokenSymbol $operationText"
    } else {
        operationText
    }
}
