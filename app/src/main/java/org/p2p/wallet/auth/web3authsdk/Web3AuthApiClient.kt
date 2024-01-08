package org.p2p.wallet.auth.web3authsdk

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.google.gson.JsonObject
import timber.log.Timber
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.p2p.core.network.environment.TorusEnvironment
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.auth.web3authsdk.mapper.Web3AuthClientMapper
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse

private const val JS_COMMUNICATION_CHANNEL_NAME = "AndroidCommunicationChannel"
private const val INDEX_HTML_URI = "file:///android_asset/index.html"

private const val TAG = "Web3AuthApiClient"

class Web3AuthApiClient(
    context: Context,
    private val torusNetwork: TorusEnvironment,
    private val mapper: Web3AuthClientMapper,
    private val gson: Gson,
    private val authRepository: AuthRepository,
    private val durationTracker: Web3AuthDurationTracker
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
            durationTracker.startMethodCall("triggerSilentSignup")
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
            durationTracker.startMethodCall("triggerSignInNoDevice")
        }
    }

    override suspend fun obtainTorusKey(googleIdJwtToken: String): String {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("obtainTorusKey triggered")

            onboardingWebView.evaluateJavascript(
                generateFacade(
                    type = "signup",
                    jsMethodCall = "obtainTorusKey('$googleIdJwtToken')"
                ),
                null
            )
            durationTracker.startMethodCall("obtainTorusKey")
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
            durationTracker.startMethodCall("triggerSignInNoCustom")
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
            durationTracker.startMethodCall("triggerSignInNoTorus")
        }
    }

    override suspend fun refreshDeviceShare(): Web3AuthSignUpResponse.ShareDetailsWithMeta {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("refreshDeviceShare triggered")

            onboardingWebView.evaluateJavascript(
                callToLastFacade(
                    jsMethodCall = "refreshDeviceShare()"
                ),
                null
            )
            durationTracker.startMethodCall("refreshDeviceShare")
        }
    }

    override suspend fun getUserMetadata(): GatewayOnboardingMetadata {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("getUserData triggered")

            onboardingWebView.evaluateJavascript(
                callToLastFacade(
                    jsMethodCall = "getUserData()"
                ),
                null
            )
            durationTracker.startMethodCall("getUserData")
        }
    }

    override suspend fun setUserMetadata(
        metadata: GatewayOnboardingMetadata
    ) {
        return suspendCancellableCoroutine {
            this.continuation = it

            Timber.tag(TAG).i("setUserData triggered")

            val metadataAsJsObject = gson.toJson(metadata)
            onboardingWebView.evaluateJavascript(
                callToLastFacade(
                    jsMethodCall = "setUserData($metadataAsJsObject)"
                ),
                null
            )
            durationTracker.startMethodCall("setUserData")
        }
    }

    private fun generateFacade(type: String, jsMethodCall: String): String {
        val isSignUp = type == "signup"
        val useNewUth = true
        val torusStorageProviderEndpoint = torusNetwork.baseUrl
        val torusVerifier = torusNetwork.verifier
        val torusSubVerifier = torusNetwork.subVerifier
        val torusNetworkEnv = torusNetwork.torusNetwork
        val torusLogLevel = torusNetwork.torusLogLevel

        if (isSignUp && userGeneratedSeedPhrase.isEmpty()) {
            runBlocking { userGeneratedSeedPhrase = authRepository.generatePhrase() }
        }

        val seedPhraseAsString = userGeneratedSeedPhrase.joinToString(separator = " ")

        return buildString {
            append("lastFacade = new p2pWeb3Auth.AndroidFacade({")
            append("type: '$type', ")
            append("torusNetwork: '$torusNetworkEnv', ")
            append("torusLoginType: 'google', ")
            append("torusEndpoint: '$torusStorageProviderEndpoint', ")
            append("torusVerifier: '$torusVerifier', ")
            if (isSignUp) {
                append("useNewEth: $useNewUth, ")
                append("privateInput: '$seedPhraseAsString', ")
            }
            if (!torusSubVerifier.isNullOrBlank()) {
                append("torusSubVerifier: '$torusSubVerifier', ")
            }
            append("logLevel: '$torusLogLevel'")
            append("}); ").also { Timber.tag(TAG).d("facade generated: $it") }
            append(callToLastFacade(jsMethodCall))
        }
    }

    private fun callToLastFacade(jsMethodCall: String): String {
        Timber.tag(TAG).d("call to lastFacade: $jsMethodCall")
        return "lastFacade.$jsMethodCall"
    }

    /**
     * All method names should be exact named as the methods in Web3Auth SDK
     */
    @Suppress("UNCHECKED_CAST")
    private inner class AndroidCommunicationChannel {
        @JavascriptInterface
        fun handleSignUpResponse(msg: String) {
            durationTracker.finishLastMethodCall()

            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignUpResponse>)
                ?.resumeWith(mapper.fromNetworkSignUp(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleSignInNoCustomResponse(msg: String) {
            durationTracker.finishLastMethodCall()

            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignInResponse>)
                ?.resumeWith(mapper.fromNetworkSignIn(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleSignInNoDeviceResponse(msg: String) {
            durationTracker.finishLastMethodCall()

            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignInResponse>)
                ?.resumeWith(mapper.fromNetworkSignIn(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleSignInNoTorusResponse(msg: String) {
            durationTracker.finishLastMethodCall()

            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignInResponse>)
                ?.resumeWith(mapper.fromNetworkSignIn(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleRefreshDeviceShare(msg: String) {
            durationTracker.finishLastMethodCall()

            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<Web3AuthSignUpResponse.ShareDetailsWithMeta>)
                ?.resumeWith(mapper.fromNetworkRefreshDeviceShare(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleGetUserData(msg: String) {
            durationTracker.finishLastMethodCall()

            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<GatewayOnboardingMetadata>)
                ?.resumeWith(mapper.fromNetworkGetUserData(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleSetUserData() {
            durationTracker.finishLastMethodCall()

            Timber.tag(TAG).d("handleSetUserData called")
            (continuation as? CancellableContinuation<Unit>)
                ?.resumeWith(Result.success(Unit))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }

        @JavascriptInterface
        fun handleError(error: String) {
            durationTracker.finishLastMethodCall(isMethodReturnedError = true)

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
            durationTracker.finishLastMethodCall()

            Timber.tag(TAG).d(msg)
            (continuation as? CancellableContinuation<String>)
                ?.resumeWith(mapper.obtainTorusKey(msg))
                ?: continuation?.resumeWithException(ClassCastException("Web3Auth continuation cast failed"))
        }
    }
}
