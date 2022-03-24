package org.p2p.wallet.common.analytics.repository

interface AnalyticsLocalRepository {

    fun onScreenChanged(screenName: String)
    fun getCurrentScreenName(): String
    fun getPreviousScreenName(): String
}
