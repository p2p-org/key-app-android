package org.p2p.wallet.send.analytics

import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_ACTION_BUTTON_CLICKED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CHANGING_CURRENCY
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CHANGING_TOKEN
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CHOOSING_RECEIPT
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_COMPLETED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CONFIRM_BUTTON_PRESSED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_CREATING_ANOTHER
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_FILLING_ADDRESS
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_GOING_BACK
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_IS_USERNAME
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_PASTING
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_PROCESS_SHOWN
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_QR_GOING_BACK
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_QR_SCANNING
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_RECIPIENT_SCREEN
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_RESOLVED_AUTO
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_RESOLVED_MANUALLY
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_REVIEWING
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_SHOWING_DETAILS
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_SHOW_DETAIL_PRESSED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_STARTED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_START_SCREEN
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_USER_CONFIRMED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_VERIFICATION_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.SEND_VIEWED
import org.p2p.core.token.Token
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SendFee
import java.math.BigDecimal

class SendAnalytics(private val tracker: Analytics) {

    var isSendMaxButtonClickedOnce: Boolean = false
    var isSendTargetUsername: Boolean = false

    fun logSendStartedScreen(lastScreenName: String) {
        tracker.logEvent(
            event = SEND_START_SCREEN,
            params = mapOf("Last_Screen" to lastScreenName)
        )
    }

    fun logSendViewed(lastScreenName: String) {
        tracker.logEvent(
            SEND_VIEWED,
            mapOf(
                "Last_Screen" to lastScreenName
            )
        )
    }

    fun logSendChangingToken(tokenName: String) {
        tracker.logEvent(
            SEND_CHANGING_TOKEN,
            mapOf(
                "Token_Name" to tokenName
            )
        )
    }

    fun logSendChangingCurrency(currencyName: String) {
        tracker.logEvent(
            SEND_CHANGING_CURRENCY,
            mapOf(
                "Send_Currency" to currencyName
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
            mapOf(
                "Send_Sum" to sendSum,
                "Send_Currency" to sendCurrency,
                "Send_MAX" to sendMax,
                "Send_USD" to sendUSD
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
            mapOf(
                "Send_Sum" to sendSum,
                "Send_Currency" to sendCurrency,
                "Send_MAX" to sendMax,
                "Send_USD" to sendUSD
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
            mapOf(
                "QR_Camera_Availability" to qrCameraIsAvailable,
                "QR_Gallery_Availability" to qrGalleryIsAvailable,
                "QR_Tab" to qrTab.title,
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
            mapOf(
                "Address_Source" to addressSource.title,
                "Send_Username" to isSendUsername
            )
        )
    }

    fun logSendResolvedManually(
        resolveOptionsNumber: Int,
        isNoFunds: Boolean
    ) {
        tracker.logEvent(
            SEND_RESOLVED_MANUALLY,
            mapOf(
                "Resolve_Options_Number" to resolveOptionsNumber,
                "No_Funds" to isNoFunds
            )
        )
    }

    fun logSendReviewing(
        sendNetwork: NetworkType,
        sendCurrency: String,
        sendSum: BigDecimal,
        sendMax: Boolean,
        sendUSD: BigDecimal,
        sendFree: Boolean,
        sendUsername: Boolean
    ) {
        tracker.logEvent(
            SEND_REVIEWING,
            mapOf(
                "Send_Network" to sendNetwork.toAnalyticsValue().title,
                "Send_Currency" to sendCurrency,
                "Send_Sum" to sendSum,
                "Send_MAX" to sendMax,
                "Send_USD" to sendUSD,
                "Send_Free" to sendFree,
                "Send_Username" to sendUsername
            )
        )
    }

    fun logSendVerificationInvoked(authType: AuthAnalytics.AuthType) {
        tracker.logEvent(
            SEND_VERIFICATION_INVOKED,
            mapOf(
                "Auth_Type" to authType.title
            )
        )
    }

    fun logSendProcessShown() {
        tracker.logEvent(SEND_PROCESS_SHOWN)
    }

    fun logSendCreatingAnother(sendStatus: SendStatus) {
        tracker.logEvent(
            SEND_CREATING_ANOTHER,
            mapOf(
                "Send_Status" to sendStatus.title
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
        sendNetwork: AnalyticsSendNetwork,
        sendSum: BigDecimal,
        sendUSD: BigDecimal
    ) {
        tracker.logEvent(
            SEND_SHOWING_DETAILS,
            mapOf(
                "Send_Status" to sendStatus.title,
                "Last_Screen" to lastScreenName,
                "Token_Name" to tokenName,
                "Send_Network" to sendNetwork.title,
                "Send_Sum" to sendSum,
                "Send_USD" to sendUSD
            )
        )
    }

    fun logUserConfirmedSend(
        networkType: NetworkType,
        sendAmount: BigDecimal,
        sendToken: Token.Active,
        fee: SendFee?,
        usdAmount: BigDecimal,
    ) {
        tracker.logEvent(
            event = SEND_USER_CONFIRMED,
            params = mapOf(
                "Send_Network" to networkType.toAnalyticsValue().title,
                "Send_Currency" to sendToken.tokenSymbol,
                "Send_Sum" to sendAmount.toString(),
                "Send_MAX" to (sendAmount == sendToken.total),
                "Send_USD" to usdAmount,
                "Send_Free" to (fee == null),
                "Send_Account_Fee_Token" to (fee?.feePayerSymbol ?: "None")
            )
        )
    }

    fun logSendStarted(
        networkType: NetworkType,
        sendAmount: BigDecimal,
        sendToken: Token.Active,
        fee: SendFee?,
        usdAmount: BigDecimal,
    ) {
        tracker.logEvent(
            event = SEND_STARTED,
            params = mapOf(
                "Send_Network" to networkType.toAnalyticsValue().title,
                "Send_Currency" to sendToken.tokenSymbol,
                "Send_Sum" to sendAmount.toString(),
                "Send_MAX" to (sendAmount == sendToken.total),
                "Send_USD" to usdAmount,
                "Send_Free" to (fee == null),
                "Send_Account_Fee_Token" to (fee?.feePayerSymbol ?: "None")
            )
        )
    }

    fun logSendCompleted(
        networkType: NetworkType,
        sendAmount: BigDecimal,
        sendToken: Token.Active,
        fee: SendFee?,
        usdAmount: BigDecimal,
    ) {
        tracker.logEvent(
            event = SEND_COMPLETED,
            params = mapOf(
                "Send_Network" to networkType.toAnalyticsValue().title,
                "Send_Currency" to sendToken.tokenSymbol,
                "Send_Sum" to sendAmount.toString(),
                "Send_MAX" to (sendAmount == sendToken.total),
                "Send_USD" to usdAmount,
                "Send_Free" to (fee == null),
                "Send_Account_Fee_Token" to (fee?.feePayerSymbol ?: "None")
            )
        )
    }

    fun logRecipientScreenOpened() {
        tracker.logEvent(event = SEND_RECIPIENT_SCREEN)
    }

    fun logFillingAddress() {
        tracker.logEvent(event = SEND_FILLING_ADDRESS)
    }

    fun logConfirmButtonPressed(
        network: NetworkType,
        sendCurrency: String,
        sendSum: String,
        sendSumInUsd: String,
        isSendFree: Boolean,
        accountFeeTokenSymbol: String?
    ) {
        tracker.logEvent(
            event = SEND_CONFIRM_BUTTON_PRESSED,
            params = mapOf(
                "Send_Network" to network.toAnalyticsValue().title,
                "Send_Currency" to sendCurrency,
                "Send_Sum" to sendSum,
                "Send_MAX" to isSendMaxButtonClickedOnce,
                "Send_USD" to sendSumInUsd,
                "Send_Free" to isSendFree,
                "Send_Username" to isSendTargetUsername,
                "Send_Account_Fee_Token" to (accountFeeTokenSymbol ?: "None")
            )
        )
    }

    fun logSendActionButtonClicked() {
        tracker.logEvent(SEND_ACTION_BUTTON_CLICKED)
    }

    fun logIsSendByUsername() {
        tracker.logEvent(
            event = SEND_IS_USERNAME,
            params = mapOf("Result" to isSendTargetUsername)
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

    enum class AnalyticsSendNetwork(val title: String) {
        SOLANA("Solana"),
        BITCOIN("Bitcoin")
    }

    private fun NetworkType.toAnalyticsValue(): AnalyticsSendNetwork = when (this) {
        NetworkType.SOLANA -> AnalyticsSendNetwork.SOLANA
        NetworkType.BITCOIN -> AnalyticsSendNetwork.BITCOIN
    }
}
