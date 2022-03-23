package org.p2p.wallet.auth.analytics.repository

interface AnalyticsLocalRepository {

    fun onScreenChanged(screenName: String)
    fun getCurrentScreenName(): String
    fun getPreviousScreenName(): String
}
