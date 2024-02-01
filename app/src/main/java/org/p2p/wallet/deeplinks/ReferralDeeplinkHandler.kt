package org.p2p.wallet.deeplinks

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.R
import org.p2p.wallet.referral.repository.ReferralRepository

class ReferralDeeplinkHandler(
    private val context: Context,
    private val referralRepository: ReferralRepository,
    private val appScope: AppScope
) {
    fun isReferralDeeplink(data: Uri): Boolean {
        val scheme = "https"
        val host = context.getString(R.string.app_host_referral)
        return data.host == host && data.scheme == scheme
    }

    fun createDeeplinkData(intent: Intent): DeeplinkData? {
        val data = intent.data ?: return null
        val target = DeeplinkTarget.REFERRAL
        return DeeplinkData(
            target = target,
            pathSegments = data.pathSegments,
            args = data.queryParameterNames
                .filter { !data.getQueryParameter(it).isNullOrBlank() }
                .associateWith { data.getQueryParameter(it)!! },
            intent = intent
        )
    }

    fun handleDeeplink(data: DeeplinkData) {
        val referrer = data.pathSegments.firstOrNull() ?: return
        appScope.launch { referralRepository.setReferent(referrer) }
    }
}
