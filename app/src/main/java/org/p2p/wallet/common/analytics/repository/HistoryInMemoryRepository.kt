package org.p2p.wallet.common.analytics.repository

import kotlinx.coroutines.flow.MutableStateFlow

class HistoryInMemoryRepository : HistoryLocalRepository {

    private val lastScreenNameFlow = MutableStateFlow("")

    override fun saveLastScreenName(screenName: String) {
        lastScreenNameFlow.value = screenName
    }

    override fun getLastScreenName(): String = lastScreenNameFlow.value
}