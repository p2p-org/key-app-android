package org.p2p.wallet.auth.common

import android.content.Context
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.google.gson.Gson
import org.p2p.wallet.auth.model.DeviceShareKey
import org.p2p.wallet.auth.model.GoogleAuthFlow
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

private const val indexUri = "file:///android_asset/index.html"
private const val communicationChannel = "AndroidCommunicationChannel"

class WalletWeb3AuthManager(
    context: Context,
    networkServicesUrlProvider: NetworkServicesUrlProvider,
    private val gson: Gson,
    private val deviceShareStorage: DeviceShareStorage,
) {

    private val torusUrl = networkServicesUrlProvider.loadTorusEnvironment().baseUrl

    private var lastUserId: String = emptyString()
    private var lastIdToken: String? = null

    val userId: String
        get() = lastUserId

    var flowMode = GoogleAuthFlow.SIGN_IN

    private val onboardingWebView: WebView

    var handlers: MutableList<Web3AuthHandler> = mutableListOf()

    init {
        onboardingWebView = WebView(context).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // TODO PWN-4362 remove or make build related after all onboarding testing completed!
                WebView.setWebContentsDebuggingEnabled(true)
            }
            settings.apply {
                javaScriptEnabled = true
                databaseEnabled = true
                domStorageEnabled = true
            }
        }
    }

    fun attach() {
        onboardingWebView.apply {
            loadUrl(indexUri)
            addJavascriptInterface(
                AndroidCommunicationChannel(context),
                communicationChannel
            )
        }
    }

    fun setIdToken(userId: String, idToken: String) {
        lastUserId = userId
        lastIdToken = idToken
        if (flowMode == GoogleAuthFlow.SIGN_UP) {
            onSignUp(idToken)
        } else {
            onSignIn(idToken)
        }
    }

    fun addHandler(handler: Web3AuthHandler) {
        handlers.add(handler)
    }

    fun removeHandler(handler: Web3AuthHandler) {
        handlers.remove(handler)
    }

    fun detach() {
        onboardingWebView.removeJavascriptInterface(communicationChannel)
    }

    private fun onSignUp(idToken: String) {
        onboardingWebView.evaluateJavascript(
            generateFacade("signup", "triggerSilentSignup('$idToken')"),
            null
        )
    }

    private fun onSignIn(idToken: String) {
        val restoreDeviceShare = if (hasDeviceShare()) {
            val deviceShareKey = getDeviceShare(lastUserId)
            gson.toJson(deviceShareKey?.share)
        } else {
            null
        }

        restoreDeviceShare?.let {
            onboardingWebView.evaluateJavascript(
                generateFacade("signin", "triggerSignInNoCustom('$idToken', $it)"),
                null
            )
        } ?: onboardingWebView.evaluateJavascript(
            generateFacade("signin", "triggerSignInNoDevice('$idToken')"),
            null
        )
    }

    fun saveDeviceShare(deviceShare: String) {
        if (deviceShareStorage.saveDeviceShare(deviceShare, lastUserId)) {
            handlers.forEach { handler ->
                handler.onSuccessSignUp()
            }
        } else {
            Timber.w("Unable to save device share $deviceShare, $lastIdToken")
        }
    }

    fun hasDeviceShare(): Boolean = deviceShareStorage.hasDeviceShare()

    fun getDeviceShare(userId: String): DeviceShareKey? = deviceShareStorage.getDeviceShare(userId)

    fun getLastDeviceShare(): DeviceShareKey? = deviceShareStorage.getLastDeviceShareUserId()?.let { userId ->
        deviceShareStorage.getDeviceShare(userId)
    }

    private fun generateFacade(type: String, method: String): String {
        val host = torusUrl
        return StringBuilder("new p2pWeb3Auth.AndroidFacade({").apply {
            append("type: '$type', ")
            append("useNewEth: true, ")
            append("torusLoginType: 'google', ")
            append("torusEndpoint: '$host:5051', ")
            append("metadataEndpoint: '$host:2222'")
            append("}).$method")
        }.toString()
    }

    inner class AndroidCommunicationChannel(private val context: Context) {
        @JavascriptInterface
        fun handleSignUpResponse(msg: String) {
            saveDeviceShare(msg)
        }

        @JavascriptInterface
        fun handleSignInNoCustomResponse(msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun handleSignInNoDeviceResponse(msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun handleError(error: String) {
            try {
                val web3AuthError = gson.fromJson(error, Web3AuthError::class.java)
                handlers.forEach {
                    it.handleError(web3AuthError)
                }
            } catch (commonError: Error) {
                Timber.w("error on Web3Auth: $error, $commonError")
            }
        }
    }
}

interface Web3AuthHandler {
    fun onSuccessSignUp()
    fun handleError(error: Web3AuthError)
}
