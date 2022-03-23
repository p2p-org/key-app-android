package org.p2p.wallet.history.repository

import org.p2p.wallet.history.model.PriceHistory

interface HistoryRepository {
    suspend fun getDailyPriceHistory(sourceToken: String, destination: String, days: Int): List<PriceHistory>
    suspend fun getHourlyPriceHistory(sourceToken: String, destination: String, hours: Int): List<PriceHistory>
}
