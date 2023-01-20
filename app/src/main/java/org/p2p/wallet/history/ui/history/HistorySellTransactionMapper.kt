package org.p2p.wallet.history.ui.history

import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

class HistorySellTransactionMapper {

    fun map(transaction: List<SellTransaction>): List<SellTransaction> {
        val now = ZonedDateTime.now()
        return transaction
            .filter { it.status == SellTransactionStatus.WAITING_FOR_DEPOSIT || it.isNotExpired(now) }
            .sortedByDescending { it.metadata.createdAt.toZonedDateTime() }
    }

    private fun SellTransaction.isNotExpired(now: ZonedDateTime): Boolean {
        val updatedAt = metadata.updatedAt.toZonedDateTime()
        val hoursDuration = Duration.between(updatedAt, now).toHours()
        return hoursDuration <= 24L
    }
}
