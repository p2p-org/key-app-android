package org.p2p.wallet.striga.kyc.sdk

import android.app.Activity
import java.util.Locale

class StrigaSdkInitParams(
    val initialAccessToken: String,
    val accessTokenProvider: suspend () -> String?,
    val userEmail: String,
    val userPhoneNumber: String,
    val withLogs: Boolean,
    val locale: Locale,
    val listeners: StrigaSdkListeners = StrigaSdkListeners()
)

class StrigaSdkListeners(
//    val errorListener: StrigaSdkErrorListener? = null,
//    val stateListener: StrigaSdkStateListener? = null,
//    val completionListener: StrigaSdkCompletionListener? = null,
//    val actionResultListener: StrigaSdkActionResultListener? = null,
//    val eventListener: StrigaSdkEventListener? = null
)

class StrigaKycSdkFacade {
    fun startKycFlow(rootActivity: Activity, initParams: StrigaSdkInitParams) = with(initParams) {
//        val tokenExpirationHandler = createTokenExpirationHandler(accessTokenProvider)

//        SNSMobileSDK.Builder(rootActivity)
//            .withConf(SNSInitConfig(userPhoneNumber, userEmail))
//            .withAccessToken(initialAccessToken, tokenExpirationHandler)
//            .withLocale(locale)
//            .withDebug(withLogs)
//            .withLogTree(StrigaSdkLogger())
//            .withAnalyticsEnabled(isAnalyticsEnabled = false)
//            .withHandlers(
//                onError = listeners.errorListener,
//                onStateChanged = listeners.stateListener,
//                onActionResult = listeners.actionResultListener,
//                onCompleted = listeners.completionListener,
//                onEvent = listeners.eventListener,
//                onSNSInstructionsView = null,
//                onUrl = null,
//            )
//            .build()
//            .launch()
    }

//    private fun createTokenExpirationHandler(accessTokenProvider: suspend () -> String?): TokenExpirationHandler =
//        object : TokenExpirationHandler {
//            // onTokenExpired is called on a non-UI thread
//            @WorkerThread
//            override fun onTokenExpired(): String? = runBlocking { accessTokenProvider.invoke() }
//        }
}
