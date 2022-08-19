package org.p2p.wallet.auth.web3authsdk

import android.content.Context
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi.Web3AuthClientHandler
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi.Web3AuthSdkInternalError
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi.Web3AuthSignUpCallback
import org.p2p.wallet.infrastructure.network.environment.TorusEnvironment
import timber.log.Timber

private const val JS_COMMUNICATION_CHANNEL_NAME = "AndroidCommunicationChannel"
private const val INDEX_HTML_URI = "file:///android_asset/index.html"

class Web3AuthApiClient(
    context: Context,
    private val torusNetwork: TorusEnvironment,
    private val mapper: Web3AuthErrorMapper
) : Web3AuthApi {

    private var handler: Web3AuthClientHandler? = null

    private val onboardingWebView: WebView = WebView(context).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // TODO PWN-4615 remove or make build related after all onboarding testing completed!
            WebView.setWebContentsDebuggingEnabled(true)
        }
        // loadUrl and addJavascriptInterface is async, so it should be called ASAP
        addJavascriptInterface(
            AndroidCommunicationChannel(context),
            JS_COMMUNICATION_CHANNEL_NAME
        )
        loadUrl(INDEX_HTML_URI)
        settings.apply {
            javaScriptEnabled = true
            databaseEnabled = true
            domStorageEnabled = true
        }
    }

    override fun triggerSilentSignUp(socialShare: String, handler: Web3AuthSignUpCallback) {
        this.handler = handler
        onboardingWebView.evaluateJavascript(
            generateFacade(type = "signup", jsMethodCall = "triggerSilentSignup('$socialShare')"),
            null
        )
    }

    override fun triggerSignInNoDevice(socialShare: String) {
        if (handler == null) Timber.i("!!! No handler attached for Web3Auth")
        onboardingWebView.evaluateJavascript(
            generateFacade("signin", jsMethodCall = "triggerSignInNoDevice('$socialShare')"),
            null
        )
    }

    override fun triggerSignInNoCustom(socialShare: String, deviceShare: String) {
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
        val torusVerifier = torusNetwork.verifier
        val metadataEndpoint = "$host:2222"

        return buildString {
            append("new p2pWeb3Auth.AndroidFacade({")
            append("type: '$type', ")
            append("useNewEth: $useNewUth, ")
            append("torusLoginType: '$torusLoginType', ")
            append("torusEndpoint: '$torusEndpoint', ")
            append("torusVerifier: '$torusVerifier', ")
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
            mapper.fromNetworkSignUp(msg)
                ?.let { (handler as Web3AuthSignUpCallback).onSuccessSignUp(it) }
                ?: kotlin.run {
                    val error = Web3AuthSdkInternalError("triggerSignUp method result parsing failed: $msg")
                    Timber.e(error)
                    handler?.handleInternalError(error)
                }
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
                handler?.handleApiError(mapper.fromNetworkError(error))
            } catch (mappingError: Error) {
                val error = Web3AuthSdkInternalError(
                    message = "Internal error on Web3Auth: $error, $mappingError",
                    cause = mappingError
                )
                Timber.w(error)
                handler?.handleInternalError(error)
            }
            handler = null
        }
    }
}
