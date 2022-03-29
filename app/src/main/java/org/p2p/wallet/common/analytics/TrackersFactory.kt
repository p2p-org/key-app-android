package org.p2p.wallet.common.analytics

import android.app.Application
import org.p2p.wallet.common.analytics.trackers.AmplitudeTracker
import org.p2p.wallet.common.analytics.trackers.AnalyticsTracker
import org.p2p.wallet.common.analytics.trackers.TimberTracker

object TrackersFactory {
    fun create(app: Application, isAnalyticsEnabled: Boolean): Set<AnalyticsTracker> = buildSet {
        if (isAnalyticsEnabled) {
            add(AmplitudeTracker(app))
        } else {
            add(TimberTracker())
        }
    }
}
