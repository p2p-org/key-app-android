package org.p2p.wallet.common.analytics

import android.app.Application

object TrackerFactory {

    fun create(app: Application, isAnalyticsEnabled: Boolean): Set<TrackerContract> =
        when (isAnalyticsEnabled) {
            true -> setOf(
                AmplitudeTracker(app)
            )
            else -> setOf(TimberTracker())
        }
}