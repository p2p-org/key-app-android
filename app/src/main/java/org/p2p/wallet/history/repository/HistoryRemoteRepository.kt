package org.p2p.wallet.history.repository

import org.p2p.wallet.main.api.CompareApi
import org.p2p.wallet.history.model.PriceHistory

class HistoryRemoteRepository(
    private val compareApi: CompareApi
) : HistoryRepository {

    companion object {
        private const val CHART_ENTRY_LIMIT = 20
    }

    override suspend fun getDailyPriceHistory(sourceToken: String, destination: String, days: Int): List<PriceHistory> {
        val response = compareApi.getDailyPriceHistory(sourceToken, destination, days)

        return if (response.message.isNullOrEmpty()) {
            response.data.list.map { PriceHistory(it.close) }
        } else emptyList()
    }

    override suspend fun getHourlyPriceHistory(
        sourceToken: String,
        destination: String,
        hours: Int
    ): List<PriceHistory> {
        val response = compareApi.getHourlyPriceHistory(sourceToken, destination, hours, CHART_ENTRY_LIMIT)
        return if (response.message.isNullOrEmpty()) {
            response.data.list.map { PriceHistory(it.close) }
        } else emptyList()
    }
}