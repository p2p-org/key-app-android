package org.p2p.wallet.common.analytics.repository

import org.p2p.wallet.utils.emptyString

class AnalyticsInMemoryRepository : AnalyticsLocalRepository {

    private val openedScreenList = mutableListOf<String>()

    override fun changeCurrentScreen(newScreenName: String) {
        // to remove duplicates when onResume is called
        if (newScreenName != getCurrentScreenName()) {
            openedScreenList.add(newScreenName)
        }
    }

    override fun getCurrentScreenName(): String =
        openedScreenList.lastOrNull()
            ?: emptyString()

    override fun getPreviousScreenName(): String =
        kotlin.runCatching {
            openedScreenList[openedScreenList.lastIndex - 1]
        }
            .getOrDefault(emptyString())
}
