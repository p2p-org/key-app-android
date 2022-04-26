package org.p2p.wallet.common.analytics

import android.app.Application
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.analytics.trackers.AmplitudeTracker
import org.p2p.wallet.common.analytics.trackers.AnalyticsTracker
import org.p2p.wallet.common.analytics.trackers.TimberTracker

object TrackersFactory {
    fun create(app: Application): Set<AnalyticsTracker> = buildSet {
        if (BuildConfig.AMPLITUDE_ENABLED) {
            add(AmplitudeTracker(app))
        }
        if (BuildConfig.DEBUG) {
            add(TimberTracker())
        }
    }
}
