package org.p2p.wallet.common.analytics

import android.app.Application
import org.p2p.core.BuildConfig.APPSFLYER_ENABLED
import org.p2p.core.BuildConfig.FIREBASE_ANALYTICS_ENABLED
import org.p2p.core.analytics.trackers.AmplitudeTracker
import org.p2p.core.analytics.trackers.AnalyticsTracker
import org.p2p.core.analytics.trackers.AppsFlyerTracker
import org.p2p.core.analytics.trackers.FirebaseAnalyticsTracker
import org.p2p.core.analytics.trackers.TimberTracker
import org.p2p.wallet.BuildConfig

object TrackersFactory {
    fun create(
        app: Application,
        amplitudeTracker: AmplitudeTracker
    ): Set<AnalyticsTracker> = buildSet {
        add(amplitudeTracker)

        if (APPSFLYER_ENABLED) {
            add(AppsFlyerTracker(app))
        }
        if (FIREBASE_ANALYTICS_ENABLED) {
            add(FirebaseAnalyticsTracker(app))
        }
        if (BuildConfig.DEBUG) {
            add(TimberTracker())
        }
    }
}
