package org.p2p.wallet.referral

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.JsonObject
import timber.log.Timber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.toBase64Instance
import org.p2p.solanaj.utils.SolanaMessageSigner
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

private typealias JsResultWrapper = String

class ReferralWebViewBridge(
    webView: WebView,
    private val tokenKeyProvider: TokenKeyProvider,
    private val onShareLinkCalled: (String) -> Unit,
    private val onWebViewLoaded: () -> Unit
) {
    companion object {
        const val JS_BRIDGE_OBJECT_NAME = "ReferralBridge"
        private const val REFERRAL_URL = "https://referral-2ii.pages.dev/"
    }

    private var referralWebView: WebView? = null

    private val onPageLoadingFinishedClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            if (view?.progress == 100) {
                onWebViewLoaded.invoke()
            }
            super.onPageFinished(view, url)
        }
    }

    init {
        referralWebView = webView
        referralWebView?.apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(BridgeInterface(), JS_BRIDGE_OBJECT_NAME)
            webViewClient = onPageLoadingFinishedClient
            loadUrl(REFERRAL_URL)
        }
    }

    fun onFragmentResumed() {
        referralWebView?.onResume()
    }

    fun onFragmentPaused() {
        referralWebView?.onPause()
    }

    fun onFragmentViewDestroyed() {
        referralWebView?.destroy()
        referralWebView = null
    }

    private inner class BridgeInterface {

        @JavascriptInterface
        fun nativeLog(message: String) {
            Timber.tag(JS_BRIDGE_OBJECT_NAME).w(message)
        }

        @JavascriptInterface
        fun signMessageAsync(messageBase64: String): JsResultWrapper = makeAsyncCall {
            try {
                val signer = SolanaMessageSigner()
                val signedMessage = signer.signMessage(
                    message = messageBase64.toBase64Instance().decodeToBytes(),
                    keyPair = tokenKeyProvider.keyPair
                )
                wrapInJsResult(signedMessage.base64Value)
            } catch (e: Throwable) {
                Timber.e(e, "Unable to evaluate JS method signMessageAsync")
                wrapInJsResult(e)
            }
        }
    }

    private fun wrapInJsResult(value: String): JsResultWrapper {
        return JsonObject()
            .apply { addProperty("value", value) }
            .toString()
    }

    private fun wrapInJsResult(error: Throwable): JsResultWrapper {
        return JsonObject()
            .apply { addProperty("error", error.message) }
            .toString()
    }

    @JavascriptInterface
    fun showShareDialog(link: String) {
        onShareLinkCalled.invoke(link)
    }

    @JavascriptInterface
    fun getUserPublicKeyAsync(): JsResultWrapper = makeAsyncCall {
        wrapInJsResult(tokenKeyProvider.publicKey)
    }

    // invoke block in IO, return result on Main thread
    private fun makeAsyncCall(block: suspend () -> String): String = runBlocking(Dispatchers.Main.immediate) {
        withContext(Dispatchers.IO) { block.invoke() }
    }
}
