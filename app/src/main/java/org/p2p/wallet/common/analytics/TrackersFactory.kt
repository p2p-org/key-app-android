package org.p2p.wallet.common.analytics

import android.app.Application
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.analytics.trackers.AmplitudeTracker
import org.p2p.wallet.common.analytics.trackers.AnalyticsTracker
import org.p2p.wallet.common.analytics.trackers.AppsFlyerTracker
import org.p2p.wallet.common.analytics.trackers.FirebaseTracker
import org.p2p.wallet.common.analytics.trackers.TimberTracker
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

object TrackersFactory {
    fun create(app: Application, tokenKeyProvider: TokenKeyProvider): Set<AnalyticsTracker> = buildSet {
        if (BuildConfig.AMPLITUDE_ENABLED) {
            add(AmplitudeTracker(app, tokenKeyProvider))
        }
        if (BuildConfig.APPSFLYER_ENABLED) {
            add(AppsFlyerTracker(app))
        }
        if (BuildConfig.FIREBASE_ANALYTICS_ENABLED) {
            add(FirebaseTracker(app))
        }
        if (BuildConfig.DEBUG) {
            add(TimberTracker())
        }
    }
}
