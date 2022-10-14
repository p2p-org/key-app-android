package org.p2p.wallet.common.analytics.repository

interface AnalyticsLocalRepository {
    fun changeCurrentScreen(newScreenName: String)
    fun getCurrentScreenName(): String
    fun getPreviousScreenName(): String
}
