package org.p2p.wallet.auth.web3authsdk

import android.content.Context
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.auth.web3authsdk.mapper.Web3AuthClientMapper
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.network.environment.TorusEnvironment
import timber.log.Timber
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine

private const val JS_COMMUNICATION_CHANNEL_NAME = "AndroidCommunicationChannel"
private const val INDEX_HTML_URI = "file:///android_asset/index.html"

private const val TAG = "Web3AuthApiClient"

class Web3AuthApiClient(
    context: Context,
    private val torusNetwork: TorusEnvironment,
    private val mapper: Web3AuthClientMapper,
    private val gson: Gson,
    private val authRepository: AuthRepository
) : Web3AuthApi {

    private var continuation: CancellableContinuation<*>? = null

    private var userGeneratedSeedPhrase: List<String> = emptyList()

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

    override suspend fun triggerSilentSignUp(socialShare: String): Web3AuthSignUpResponse {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("triggerSilentSignUp triggered")

            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signup",
                    jsMethodCall = "triggerSilentSignup('$socialShare')"
                ),
                null
            )
        }
    }

    override suspend fun triggerSignInNoDevice(
        socialShare: String,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
    ): Web3AuthSignInResponse {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("triggerSignInNoDevice triggered")

            val thirdShareAsJsObject = gson.toJson(thirdShare)
            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signin",
                    jsMethodCall = "triggerSignInNoDevice('$socialShare', $thirdShareAsJsObject)"
                ),
                null
            )
        }
    }

    override suspend fun triggerSignInNoCustom(
        socialShare: String,
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
    ): Web3AuthSignInResponse {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("triggerSignInNoCustom triggered")

            val deviceShareAsJsObject = gson.toJson(deviceShare)
            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signin",
                    jsMethodCall = "triggerSignInNoCustom('$socialShare', $deviceShareAsJsObject)"
                ),
                null
            )
        }
    }

    override suspend fun triggerSignInNoTorus(
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        encryptedMnemonicPhrase: JsonObject,
    ): Web3AuthSignInResponse {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("triggerSignInNoTorus triggered")

            val thirdShareAsJsObject = gson.toJson(thirdShare)
            val deviceShareAsJsObject = gson.toJson(deviceShare)

            val params = "$deviceShareAsJsObject, $thirdShareAsJsObject, $encryptedMnemonicPhrase"

            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signin",
                    jsMethodCall = "triggerSignInNoTorus($params)"
                ),
                null
            )
        }
    }

    private fun generateFacade(type: String, jsMethodCall: String): String {
        val host = torusNetwork.baseUrl
        val useNewUth = true
        val torusLoginType = "google"
        val torusNetworkEnv = "testnet"
        val torusEndpoint = "$host:5051"
        val torusVerifier = torusNetwork.verifier

        if (userGeneratedSeedPhrase.isEmpty()) {
            runBlocking { userGeneratedSeedPhrase = authRepository.generatePhrase() }
        }
        
        val seedPhraseAsString = userGeneratedSeedPhrase.joinToString(separator = " ")

        return buildString {
            append("new p2pWeb3Auth.AndroidFacade({")
            append("type: '$type', ")
            append("useNewEth: $useNewUth, ")
            append("torusNetwork: '$torusNetworkEnv', ")
            append("torusLoginType: '$torusLoginType', ")
            append("torusEndpoint: '$torusEndpoint', ")
            append("torusVerifier: '$torusVerifier', ")
            append("privateInput: '$seedPhraseAsString'")
            append("})")
            append(".$jsMethodCall")
        }
            .also { Timber.tag(TAG).i("facade generated: $it") }
    }

    /**
     * All method names should be exact named as the methods in Web3Auth SDK
     */
    private inner class AndroidCommunicationChannel {
        @JavascriptInterface
        fun handleSignUpResponse(msg: String) {
            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignUpResponse>)
                ?.resumeWith(mapper.fromNetworkSignUp(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleSignInNoCustomResponse(msg: String) {
            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignInResponse>)
                ?.resumeWith(mapper.fromNetworkSignIn(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleSignInNoDeviceResponse(msg: String) {
            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignInResponse>)
                ?.resumeWith(mapper.fromNetworkSignIn(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleSignInNoTorusResponse(msg: String) {
            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignInResponse>)
                ?.resumeWith(mapper.fromNetworkSignIn(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleError(error: String) {
            Timber.tag(TAG).d(error)
            runCatching<Throwable> { mapper.fromNetworkError(error) }
                .recover { it }
                .onSuccess { continuation?.resumeWithException(it) }
        }
    }
}
