package org.p2p.wallet.auth.common

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.google.gson.Gson
import org.p2p.wallet.auth.model.DeviceShareKey
import org.p2p.wallet.auth.model.GoogleAuthFlow
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import timber.log.Timber

private const val KEY_DEVICE_SHARE = "KEY_DEVICE_SHARE"
private const val indexUri = "file:///android_asset/index.html"
private const val communicationChannel = "AndroidCommunicationChannel"

class WalletWeb3AuthManager(
    context: Context,
    private val gson: Gson,
    private val secureStorage: SecureStorageContract,
    private val sharedPreferences: SharedPreferences,
) {

    private var lastUserId: String? = null
    private var lastIdToken: String? = null

    var flowMode = GoogleAuthFlow.SIGN_IN

    private val onboardingWebView: WebView

    var errorHandler: Web3AuthErrorHandler? = null

    init {
        onboardingWebView = WebView(context).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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

    fun detach() {
        onboardingWebView.removeJavascriptInterface(communicationChannel)
    }

    private fun onSignUp(idToken: String) {
        onboardingWebView.evaluateJavascript(
            "new p2pWeb3Auth.AndroidFacade({ useRandomPrivates: true }).triggerSilentSignup('$idToken')",
            null
        )
    }

    private fun onSignIn(idToken: String) {
        val restoreDeviceShare = if (hasDeviceShare()) {
            val deviceShareKey = getDeviceShare()
            gson.toJson(deviceShareKey?.share)
        } else {
            null
        }

        restoreDeviceShare?.let {
            onboardingWebView.evaluateJavascript(
                "new p2pWeb3Auth.AndroidFacade().triggerSignInNoCustom('$idToken', $it)",
                null
            )
        } ?: onboardingWebView.evaluateJavascript(
            "new p2pWeb3Auth.AndroidFacade().triggerSignInNoDevice('$idToken')",
            null
        )
    }

    fun saveDeviceShare(deviceShare: String) {
        val share = parseDeviceShare(deviceShare)
        share?.let {
            it.userId = lastUserId
            secureStorage.saveString(KEY_DEVICE_SHARE, gson.toJson(it))
        }
    }

    private fun parseDeviceShare(deviceShare: String): DeviceShareKey? {
        return gson.fromJson(deviceShare, DeviceShareKey::class.java)
    }

    fun hasDeviceShare(): Boolean = with(sharedPreferences) {
        contains(KEY_DEVICE_SHARE)
    }

    fun getDeviceShare(): DeviceShareKey? = secureStorage.getString(KEY_DEVICE_SHARE)?.let { share ->
        parseDeviceShare(share)
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
                errorHandler?.handleError(web3AuthError)
            } catch (commonError: Error) {
                Timber.w("error on Web3Auth: $error, $commonError")
            }
        }
    }
}

interface Web3AuthErrorHandler {
    fun handleError(error: Web3AuthError)
}
