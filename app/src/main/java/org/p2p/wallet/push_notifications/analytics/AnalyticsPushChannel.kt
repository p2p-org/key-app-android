package org.p2p.wallet.push_notifications.analytics

import org.p2p.wallet.common.analytics.Analytics

private const val PUSH_PERMISSIONS_ALLOWED = "Push_Allowed"
private const val PUSH_DELIVERED = "Push_Delivered"
private const val PUSH_OPENED = "Push_Opened"

enum class PushChannel(val value: String) {
    BACKEND("Backend_Push_Service"), INTERCOM("Intercom")
}

class AnalyticsPushChannel(private val tracker: Analytics) {

    fun pushPermissionsAllowed() {
        tracker.logEvent(
            event = PUSH_PERMISSIONS_ALLOWED
        )
    }

    fun pushDelivered(pushId: String?, channel: PushChannel, campaign: String?) {
        tracker.logEvent(
            event = PUSH_DELIVERED,
            params = mapOf(
                "Push_id" to pushId.orEmpty(),
                "Channel" to channel.value,
                "Campaign" to campaign.orEmpty(),
            )
        )
    }

    fun pushOpened(pushId: String?, channel: PushChannel, campaign: String?) {
        tracker.logEvent(
            event = PUSH_OPENED,
            params = mapOf(
                "Push_id" to pushId.orEmpty(),
                "Channel" to channel.value,
                "Campaign" to campaign.orEmpty(),
            )
        )
    }
}
