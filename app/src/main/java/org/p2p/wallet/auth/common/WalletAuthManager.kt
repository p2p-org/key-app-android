package org.p2p.wallet.auth.common

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.google.gson.Gson
import org.p2p.wallet.auth.model.DeviceShareKey
import org.p2p.wallet.auth.model.GoogleFlow
import org.p2p.wallet.infrastructure.security.SecureStorageContract

private const val KEY_DEVICE_SHARE = "KEY_DEVICE_SHARE"
private const val indexUrl = "file:///android_asset/index.html"
private const val androidCommunicationChannel = "AndroidCommunicationChannel"

class WalletAuthManager(
    context: Context,
    private val gson: Gson,
    private val secureStorage: SecureStorageContract,
    private val sharedPreferences: SharedPreferences,
) {

    private var lastUserId: String? = null
    private var lastIdToken: String? = null

    var flowMode = GoogleFlow.SIGN_IN

    private val onboardingWebView = WebView(context).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        settings.javaScriptEnabled = true
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
    }

    fun attach() {
        onboardingWebView.apply {
            loadUrl(indexUrl)
            addJavascriptInterface(
                AndroidCommunicationChannel(context),
                androidCommunicationChannel
            )
        }
    }

    fun setIdToken(userId: String, idToken: String) {
        lastUserId = userId
        lastIdToken = idToken
        if (flowMode == GoogleFlow.SIGN_UP) {
            onSignUp(idToken)
        } else {
            if (hasDeviceShare()) {
                val deviceShareKey = gson.fromJson(getDeviceShare(), DeviceShareKey::class.java)
                val restoreDeviceShare = gson.toJson(deviceShareKey.share)
                onSignIn(idToken, restoreDeviceShare)
            } else {
                onSignIn(idToken)
            }
        }
    }

    fun detach() {
        onboardingWebView.removeJavascriptInterface(androidCommunicationChannel)
    }

    fun onSignUp(idToken: String) {
        onboardingWebView.evaluateJavascript(
            "new p2pWeb3Auth.AndroidFacade({ useRandomPrivates: true }).triggerSilentSignup('$idToken')",
            null
        )
    }

    fun onSignIn(idToken: String, deviceShare: String? = null) {
        deviceShare
          ?.let { "new p2pWeb3Auth.AndroidFacade().triggerSignInNoCustom('$idToken', $it)" } 
          ?: "new p2pWeb3Auth.AndroidFacade().triggerSignInNoDevice('$idToken')"
          ?.also { onboardingWebView.evaluateJavascript(it, null) } 
    }

    fun saveDeviceShare(deviceShare: String) {
        secureStorage.saveString("${KEY_DEVICE_SHARE}_$lastUserId", deviceShare)
    }

    fun hasDeviceShare() = with(sharedPreferences) {
        contains("${KEY_DEVICE_SHARE}_$lastUserId")
    }

    fun getDeviceShare() = secureStorage.getString("${KEY_DEVICE_SHARE}_$lastUserId").orEmpty()

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
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }
}
