package org.p2p.core.analytics.repository

interface AnalyticsLocalRepository {
    fun changeCurrentScreen(newScreenName: String)
    fun getCurrentScreenName(): String
    fun getPreviousScreenName(): String
}
