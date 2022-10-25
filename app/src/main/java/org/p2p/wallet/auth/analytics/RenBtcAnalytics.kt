package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.Analytics

private const val REN_BTC_CREATION = "Renbtc_Creation"
private const val REN_BTC_RECEIVE = "Renbtc_Receive"
private const val REN_BTC_SEND = "Renbtc_Send"

class RenBtcAnalytics(private val tracker: Analytics) {
    fun logRenBtcAccountCreated(creationSuccess: Boolean) {
        tracker.logEvent(
            event = REN_BTC_CREATION,
            params = mapOf(
                "Result" to creationSuccess.toString()
            )
        )
    }

    fun logRenBtcReceived(receiveSuccess: Boolean) {
        tracker.logEvent(
            event = REN_BTC_RECEIVE,
            params = mapOf(
                "Result" to receiveSuccess.toString()
            )
        )
    }

    fun logRenBtcSend(sendSuccess: Boolean) {
        tracker.logEvent(
            event = REN_BTC_SEND,
            params = mapOf(
                "Result" to sendSuccess.toString()
            )
        )
    }
}
