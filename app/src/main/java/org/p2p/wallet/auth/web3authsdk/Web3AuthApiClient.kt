package org.p2p.wallet.auth.web3authsdk

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.p2p.wallet.BuildConfig
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
    private val authRepository: AuthRepository,
    private val torusNetworkEnv: String,
    private val torusLogLevel: String,
) : Web3AuthApi {

    private var continuation: CancellableContinuation<*>? = null

    private var userGeneratedSeedPhrase: List<String> = emptyList()

    private val onboardingWebView: WebView = WebView(context).apply {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
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

    override suspend fun triggerSilentSignUp(torusKey: String): Web3AuthSignUpResponse {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("triggerSilentSignUp triggered")

            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signup",
                    jsMethodCall = "triggerSilentSignup('$torusKey')"
                ),
                null
            )
        }
    }

    override suspend fun triggerSignInNoDevice(
        torusKey: String,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        encryptedMnemonic: JsonObject,
    ): Web3AuthSignInResponse {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("triggerSignInNoDevice triggered")

            val thirdShareAsJsObject = gson.toJson(thirdShare)
            val params = "'$torusKey', $thirdShareAsJsObject, $encryptedMnemonic"
            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signin",
                    jsMethodCall = "triggerSignInNoDevice($params)"
                ),
                null
            )
        }
    }

    override suspend fun obtainTorusKey(googleUserToken: String): String {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("obtainTorusKey triggered")

            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signup",
                    jsMethodCall = "obtainTorusKey('$googleUserToken')"
                ),
                null
            )
        }
    }

    override suspend fun triggerSignInNoCustom(
        torusKey: String,
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta
    ): Web3AuthSignInResponse {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("triggerSignInNoCustom triggered")

            val deviceShareAsJsObject = gson.toJson(deviceShare)
            val params = "'$torusKey', $deviceShareAsJsObject"
            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signin",
                    jsMethodCall = "triggerSignInNoCustom($params)"
                ),
                null
            )
        }
    }

    override suspend fun triggerSignInNoTorus(
        deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        encryptedMnemonic: JsonObject,
    ): Web3AuthSignInResponse {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("triggerSignInNoTorus triggered")

            val thirdShareAsJsObject = gson.toJson(thirdShare)
            val deviceShareAsJsObject = gson.toJson(deviceShare)

            val params = "$deviceShareAsJsObject, $thirdShareAsJsObject, $encryptedMnemonic"
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
        val useNewUth = true
        val torusLoginType = "google"
        val torusEndpoint = torusNetwork.baseUrl
        val torusVerifier = torusNetwork.verifier
        val torusSubVerifier = torusNetwork.subVerifier

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
            append("logLevel: '$torusLogLevel', ")
            if (!torusSubVerifier.isNullOrBlank()) {
                append("torusSubVerifier: '$torusSubVerifier', ")
            }
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
                .onSuccess {
                    Timber.tag(TAG).e(it, "Web3Auth returned error")
                    continuation?.resumeWithException(it)
                }
        }

        @JavascriptInterface
        fun handleTorusKey(msg: String) {
            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<String>)
                ?.resumeWith(mapper.obtainTorusKey(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }
    }
}
