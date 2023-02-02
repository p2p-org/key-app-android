package org.p2p.wallet.common.analytics

import android.app.Application
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.analytics.trackers.AmplitudeTracker
import org.p2p.wallet.common.analytics.trackers.AnalyticsTracker
import org.p2p.wallet.common.analytics.trackers.AppsFlyerTracker
import org.p2p.wallet.common.analytics.trackers.TimberTracker
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

object TrackersFactory {
    fun create(app: Application, tokenKeyProvider: TokenKeyProvider): Set<AnalyticsTracker> = buildSet {
        add(AmplitudeTracker(app, tokenKeyProvider))

        if (BuildConfig.APPSFLYER_ENABLED) {
            add(AppsFlyerTracker(app, AppsFlyerLib.getInstance()))
        }
        if (BuildConfig.FIREBASE_ANALYTICS_ENABLED) {
            add(FirebaseAnalyticsTracker(FirebaseAnalytics.getInstance(app)))
        }
        if (BuildConfig.DEBUG) {
            add(TimberTracker())
        }
    }
}
