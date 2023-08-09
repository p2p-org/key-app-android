package org.p2p.wallet.notification

import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.find

class FcmPushNotificationData(
    val title: String,
    val body: String,
    val type: NotificationType = NotificationType.DEFAULT
)

enum class NotificationType(val type: String) {
    DEFAULT(emptyString()),
    RECEIVE("Received");

    companion object {
        fun fromValue(value: String): NotificationType = NotificationType::type.find(value) ?: DEFAULT
    }
}
