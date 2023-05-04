package org.p2p.wallet.history.model.bridge

import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.history.model.HistoryTransaction

sealed class BridgeHistoryTransaction() : HistoryTransaction(), Parcelable {

    @Parcelize
    data class Claim(
        val bundleId: String,
        override val date: ZonedDateTime
    ) : BridgeHistoryTransaction() {

        override fun getHistoryTransactionId(): String {
            return bundleId
        }
    }

    @Parcelize
    data class Send(
        val id: String,
        override val date: ZonedDateTime
    ) : BridgeHistoryTransaction() {

        override fun getHistoryTransactionId(): String {
            return id
        }
    }
}
