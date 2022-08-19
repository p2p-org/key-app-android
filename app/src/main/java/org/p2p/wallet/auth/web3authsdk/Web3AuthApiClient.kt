package org.p2p.wallet.auth.web3authsdk

import android.content.Context
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi.Web3AuthClientHandler
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi.Web3AuthSdkInternalError
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi.Web3AuthSignInCallback
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi.Web3AuthSignUpCallback
import org.p2p.wallet.infrastructure.network.environment.TorusEnvironment
import timber.log.Timber

private const val JS_COMMUNICATION_CHANNEL_NAME = "AndroidCommunicationChannel"
private const val INDEX_HTML_URI = "file:///android_asset/index.html"

class Web3AuthApiClient(
    context: Context,
    private val torusNetwork: TorusEnvironment,
    private val errorMapper: Web3AuthErrorMapper,
    private val gson: Gson
) : Web3AuthApi {

    private var handler: Web3AuthClientHandler? = null

    private val onboardingWebView: WebView = WebView(context).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // TODO PWN-4615 remove or make build related after all onboarding testing completed!
            WebView.setWebContentsDebuggingEnabled(true)
        }
        // loadUrl and addJavascriptInterface is async, so it should be called ASAP
        addJavascriptInterface(
            AndroidCommunicationChannel(),
            JS_COMMUNICATION_CHANNEL_NAME
        )
        loadUrl(INDEX_HTML_URI)
        settings.apply {
            javaScriptEnabled = true
            databaseEnabled = true
            domStorageEnabled = true
        }
    }

    override fun triggerSilentSignUp(
        socialShare: String,
        handler: Web3AuthSignUpCallback
    ) {
        this.handler = handler
        onboardingWebView.evaluateJavascript(
            generateFacade(
                type = "signup",
                jsMethodCall = "triggerSilentSignup('$socialShare')"
            ),
            null
        )
    }

    override fun triggerSignInNoDevice(
        socialShare: String,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        handler: Web3AuthSignInCallback
    ) {
        this.handler = handler

        val thirdShareAsJsObject = gson.toJson(thirdShare)
        onboardingWebView.evaluateJavascript(
            generateFacade(
                type = "signin",
                jsMethodCall = "triggerSignInNoDevice('$socialShare', $thirdShareAsJsObject)"
            ),
            null
        )
    }

    override fun triggerSignInNoCustom(
        socialShare: String,
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        handler: Web3AuthSignInCallback
    ) {
        this.handler = handler
        val deviceShareAsJsObject = gson.toJson(deviceShare)
        onboardingWebView.evaluateJavascript(
            generateFacade(
                type = "signin",
                jsMethodCall = "triggerSignInNoCustom('$socialShare', $deviceShareAsJsObject)"
            ),
            null
        )
    }

    override fun triggerSignInNoTorus(
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        encryptedMnemonicPhrase: JsonObject,
        handler: Web3AuthSignInCallback
    ) {
        this.handler = handler
        val thirdShareAsJsObject = gson.toJson(thirdShare)
        val deviceShareAsJsObject = gson.toJson(deviceShare)

        val params = "$deviceShareAsJsObject, $thirdShareAsJsObject, $encryptedMnemonicPhrase"

        onboardingWebView.evaluateJavascript(
            generateFacade(
                type = "signin",
                jsMethodCall = "triggerSignInNoCustom($params)"
            ),
            null
        )
    }

    private fun generateFacade(type: String, jsMethodCall: String): String {
        val host = torusNetwork.baseUrl
        val useNewUth = true
        val torusLoginType = "google"
        val torusNetworkEnv = "testnet"
        val torusEndpoint = "$host:5051"
        val torusVerifier = torusNetwork.verifier
        val metadataEndpoint = "$host:2222"

        return buildString {
            append("new p2pWeb3Auth.AndroidFacade({")
            append("type: '$type', ")
            append("useNewEth: $useNewUth, ")
            append("torusNetwork: '$torusNetworkEnv', ")
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
    private inner class AndroidCommunicationChannel {
        @JavascriptInterface
        fun handleSignUpResponse(msg: String) {
            errorMapper.fromNetworkSignUp(msg)
                ?.let { (handler as? Web3AuthSignUpCallback)?.onSuccessSignUp(it) }
                ?: handleMapperError(msg)
            handler = null
        }

        @JavascriptInterface
        fun handleSignInNoCustomResponse(msg: String) {
            errorMapper.fromNetworkSignIn(msg)
                ?.let { (handler as? Web3AuthSignInCallback)?.onSuccessSignIn(it) }
                ?: handleMapperError(msg)
            handler = null
        }

        @JavascriptInterface
        fun handleSignInNoDeviceResponse(msg: String) {
            errorMapper.fromNetworkSignIn(msg)
                ?.let { (handler as? Web3AuthSignInCallback)?.onSuccessSignIn(it) }
                ?: handleMapperError(msg)
            handler = null
        }

        @JavascriptInterface
        fun handleSignInNoTorusResponse(msg: String) {
            errorMapper.fromNetworkSignIn(msg)
                ?.let { (handler as? Web3AuthSignInCallback)?.onSuccessSignIn(it) }
                ?: handleMapperError(msg)
            handler = null
        }

        @JavascriptInterface
        fun handleError(error: String) {
            errorMapper.fromNetworkError(error)
                ?.let { handler?.handleApiError(it) }
                ?: handleMapperError(error)
            handler = null
        }

        private fun handleMapperError(originalResponse: String) {
            val error = Web3AuthSdkInternalError("Web3Auth SDK method result parsing failed: $originalResponse")
            Timber.e(error)
            handler?.handleInternalError(error)
        }
    }
}
