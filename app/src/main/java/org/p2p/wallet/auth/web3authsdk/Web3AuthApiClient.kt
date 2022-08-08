package org.p2p.wallet.auth.web3authsdk

import android.content.Context
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.google.gson.Gson
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.network.environment.TorusEnvironment
import org.p2p.wallet.utils.fromJsonReified
import timber.log.Timber

private const val JS_COMMUNICATION_CHANNEL_NAME = "AndroidCommunicationChannel"
private const val INDEX_HTML_URI = "file:///android_asset/index.html"

class Web3AuthApiClient(
    context: Context,
    private val torusNetwork: TorusEnvironment,
    private val gson: Gson
) {
    class Web3AuthSdkInternalError(override val message: String, override val cause: Throwable? = null) : Throwable()

    interface Web3AuthClientHandler {
        fun handleError(error: Web3AuthErrorResponse)
    }

    interface Web3AuthSignUpCallback : Web3AuthClientHandler {
        fun onSuccessSignUp(signUpResponse: Web3AuthSignUpResponse)
    }

    private var handler: Web3AuthClientHandler? = null

    private val onboardingWebView: WebView = WebView(context).apply {
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

    fun attach() {
        onboardingWebView.apply {
            loadUrl(INDEX_HTML_URI)
            addJavascriptInterface(
                AndroidCommunicationChannel(context),
                JS_COMMUNICATION_CHANNEL_NAME
            )
        }
    }

    fun detach() {
        onboardingWebView.removeJavascriptInterface(JS_COMMUNICATION_CHANNEL_NAME)
    }

    fun triggerSilentSignUp(socialShare: String, handler: Web3AuthSignUpCallback) {
        this.handler = handler
        onboardingWebView.evaluateJavascript(
            generateFacade(type = "signup", jsMethodCall = "triggerSilentSignup('$socialShare')"),
            null
        )
    }

    fun triggerSignInNoDevice(socialShare: String) {
        if (handler == null) Timber.i("!!! No handler attached for Web3Auth")
        onboardingWebView.evaluateJavascript(
            generateFacade("signin", jsMethodCall = "triggerSignInNoDevice('$socialShare')"),
            null
        )
    }

    fun triggerSignInNoCustom(socialShare: String, deviceShare: String) {
        if (handler == null) Timber.i("!!! No handler attached for Web3Auth")
        onboardingWebView.evaluateJavascript(
            generateFacade(type = "signin", jsMethodCall = "triggerSignInNoCustom('$socialShare', $deviceShare)"),
            null
        )
    }

    private fun generateFacade(type: String, jsMethodCall: String): String {
        val host = torusNetwork.baseUrl
        val useNewUth = true
        val torusLoginType = "google"
        val torusEndpoint = "$host:5051"
        val metadataEndpoint = "$host:2222"

        return buildString {
            append("new p2pWeb3Auth.AndroidFacade({")
            append("type: '$type', ")
            append("useNewEth: $useNewUth, ")
            append("torusLoginType: '$torusLoginType', ")
            append("torusEndpoint: '$torusEndpoint', ")
            append("metadataEndpoint: '$metadataEndpoint'")
            append("})")
            append(".$jsMethodCall")
        }
    }

    /**
     * All method names should be exact named as the methods in Web3Auth SDK
     */
    private inner class AndroidCommunicationChannel(private val context: Context) {
        @JavascriptInterface
        fun handleSignUpResponse(msg: String) {
            gson.fromJsonReified<Web3AuthSignUpResponse>(msg)
                ?.let { (handler as Web3AuthSignUpCallback).onSuccessSignUp(it) }
                ?: Timber.e(Web3AuthSdkInternalError("triggerSignUp method result parsing failed: $msg"))

            handler = null
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
                handler?.handleError(gson.fromJsonReified<Web3AuthErrorResponse>(error)!!)
            } catch (commonError: Error) {
                Timber.w(Web3AuthSdkInternalError("Internal error on Web3Auth: $error, $commonError", commonError))
            }
        }
    }
}
