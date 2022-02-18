package org.p2p.wallet.send.analytics

import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.TrackerContract
import java.math.BigDecimal

class SendAnalytics(private val tracker: TrackerContract) {

    fun logSendViewed(lastScreenName: String) {
        tracker.logEvent(
            "Send_Viewed",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logSendChangingToken(tokenName: String) {
        tracker.logEvent(
            "Send_Changing_Token",
            arrayOf(
                Pair("Token_Name", tokenName)
            )
        )
    }

    fun logSendChangingCurrency(currencyName: String) {
        tracker.logEvent(
            "Send_Changing_Currency",
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
            "Send_Going_Back",
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
            "Send_Choosing_Recipient",
            arrayOf(
                Pair("Send_Sum", sendSum),
                Pair("Send_Currency", sendCurrency),
                Pair("Send_MAX", sendMax),
                Pair("Send_USD", sendUSD)
            )
        )
    }

    fun logSendQrScanning() {
        tracker.logEvent("Send_QR_Scanning")
    }

    fun logSendQrGoingBack(
        qrCameraIsAvailable: Boolean,
        qrGalleryIsAvailable: Boolean,
        qrTab: QrTab
    ) {
        tracker.logEvent(
            "Send_QR_Going_Back",
            arrayOf(
                Pair("QR_Camera_Availability", qrCameraIsAvailable),
                Pair("QR_Gallery_Availability", qrGalleryIsAvailable),
                Pair("QR_Tab", qrTab.title),
            )
        )
    }

    fun logSendPasting() {
        tracker.logEvent("Send_Pasting")
    }

    fun logSendResolvedAuto(
        addressSource: AddressSource,
        isSendUsername: Boolean
    ) {
        tracker.logEvent(
            "Send_Resolved_Auto",
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
            "Send_Resolved_Manually",
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
            "Send_Reviewing",
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
            "Send_Verification_Invoked",
            arrayOf(
                Pair("Auth_Type", authType.title)
            )
        )
    }

    fun logSendProcessShown() {
        tracker.logEvent("Send_Process_Shown")
    }

    fun logSendCreatingAnother(sendStatus: SendStatus) {
        tracker.logEvent(
            "Send_Creating_Another",
            arrayOf(
                Pair("Send_Status", sendStatus.title)
            )
        )
    }

    fun logSendShowDetailsPressed() {
        tracker.logEvent("Send_Show_Details_Pressed")
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
            "Send_Showing_Details",
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