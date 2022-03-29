package org.p2p.wallet.send.analytics

import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CHANGING_CURRENCY
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CHANGING_TOKEN
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CHOOSING_RECEIPT
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CREATING_ANOTHER
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_GOING_BACK
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_PASTING
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_PROCESS_SHOWN
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_QR_GOING_BACK
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_QR_SCANNING
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_RESOLVED_AUTO
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_RESOLVED_MANUALLY
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_REVIEWING
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_SHOWING_DETAILS
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_SHOW_DETAIL_PRESSED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_VERIFICATION_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_VIEWED
import java.math.BigDecimal

class SendAnalytics(private val tracker: Analytics) {

    fun logSendViewed(lastScreenName: String) {
        tracker.logEvent(
            SEND_VIEWED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logSendChangingToken(tokenName: String) {
        tracker.logEvent(
            SEND_CHANGING_TOKEN,
            arrayOf(
                Pair("Token_Name", tokenName)
            )
        )
    }

    fun logSendChangingCurrency(currencyName: String) {
        tracker.logEvent(
            SEND_CHANGING_CURRENCY,
            arrayOf(
                Pair("Send_Currency", currencyName)
            )
        )
    }

    fun logSendGoingBack(
        sendSum: BigDecimal,
        sendCurrency: String,
        sendUSD: BigDecimal,
        sendMax: Boolean
    ) {
        tracker.logEvent(
            SEND_GOING_BACK,
            arrayOf(
                Pair("Send_Sum", sendSum),
                Pair("Send_Currency", sendCurrency),
                Pair("Send_MAX", sendMax),
                Pair("Send_USD", sendUSD)
            )
        )
    }

    fun logSendChoosingRecipient(
        sendSum: BigDecimal,
        sendCurrency: String,
        sendUSD: BigDecimal,
        sendMax: Boolean
    ) {
        tracker.logEvent(
            SEND_CHOOSING_RECEIPT,
            arrayOf(
                Pair("Send_Sum", sendSum),
                Pair("Send_Currency", sendCurrency),
                Pair("Send_MAX", sendMax),
                Pair("Send_USD", sendUSD)
            )
        )
    }

    fun logSendQrScanning() {
        tracker.logEvent(SEND_QR_SCANNING)
    }

    fun logSendQrGoingBack(
        qrCameraIsAvailable: Boolean,
        qrGalleryIsAvailable: Boolean,
        qrTab: QrTab
    ) {
        tracker.logEvent(
            SEND_QR_GOING_BACK,
            arrayOf(
                Pair("QR_Camera_Availability", qrCameraIsAvailable),
                Pair("QR_Gallery_Availability", qrGalleryIsAvailable),
                Pair("QR_Tab", qrTab.title),
            )
        )
    }

    fun logSendPasting() {
        tracker.logEvent(SEND_PASTING)
    }

    fun logSendResolvedAuto(
        addressSource: AddressSource,
        isSendUsername: Boolean
    ) {
        tracker.logEvent(
            SEND_RESOLVED_AUTO,
            arrayOf(
                Pair("Address_Source", addressSource.title),
                Pair("Send_Username", isSendUsername)
            )
        )
    }

    fun logSendResolvedManually(
        resolveOptionsNumber: Int,
        isNoFunds: Boolean
    ) {
        tracker.logEvent(
            SEND_RESOLVED_MANUALLY,
            arrayOf(
                Pair("Resolve_Options_Number", resolveOptionsNumber),
                Pair("No_Funds", isNoFunds)
            )
        )
    }

    fun logSendReviewing(
        sendNetwork: SendNetwork,
        sendCurrency: String,
        sendSum: BigDecimal,
        sendMax: Boolean,
        sendUSD: BigDecimal,
        sendFree: Boolean,
        sendUsername: Boolean
    ) {
        tracker.logEvent(
            SEND_REVIEWING,
            arrayOf(
                Pair("Send_Network", sendNetwork),
                Pair("Send_Currency", sendCurrency),
                Pair("Send_Sum", sendSum),
                Pair("Send_MAX", sendMax),
                Pair("Send_USD", sendUSD),
                Pair("Send_Free", sendFree),
                Pair("Send_Username", sendUsername)
            )
        )
    }

    fun logSendVerificationInvoked(authType: AuthAnalytics.AuthType) {
        tracker.logEvent(
            SEND_VERIFICATION_INVOKED,
            arrayOf(
                Pair("Auth_Type", authType.title)
            )
        )
    }

    fun logSendProcessShown() {
        tracker.logEvent(SEND_PROCESS_SHOWN)
    }

    fun logSendCreatingAnother(sendStatus: SendStatus) {
        tracker.logEvent(
            SEND_CREATING_ANOTHER,
            arrayOf(
                Pair("Send_Status", sendStatus.title)
            )
        )
    }

    fun logSendShowDetailsPressed() {
        tracker.logEvent(SEND_SHOW_DETAIL_PRESSED)
    }

    fun logSendShowingDetails(
        sendStatus: SendStatus,
        lastScreenName: String,
        tokenName: String,
        sendNetwork: SendNetwork,
        sendSum: BigDecimal,
        sendUSD: BigDecimal
    ) {
        tracker.logEvent(
            SEND_SHOWING_DETAILS,
            arrayOf(
                Pair("Send_Status", sendStatus.title),
                Pair("Last_Screen", lastScreenName),
                Pair("Token_Name", tokenName),
                Pair("Send_Network", sendNetwork.title),
                Pair("Send_Sum", sendSum),
                Pair("Send_USD", sendUSD)
            )
        )
    }

    enum class QrTab(val title: String) {
        CAMERA("Camera"),
        GALLERY("Gallery")
    }

    enum class AddressSource(val title: String) {
        CAMERA("Camera"),
        GALLERY("Gallery"),
        PASTE("Paste"),
        TYPING("Typing")
    }

    enum class SendStatus(val title: String) {
        SUCCESS("Success"),
        PENDING("Pending"),
        ERROR("Error")
    }

    enum class SendNetwork(val title: String) {
        SOLANA("Solana"),
        BITCOIN("Bitcoin")
    }
}
