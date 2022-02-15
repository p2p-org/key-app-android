package org.p2p.wallet.common.analytics.repository

interface HistoryLocalRepository {

    fun saveLastScreenName(screenName: String)

    fun getLastScreenName(): String
}