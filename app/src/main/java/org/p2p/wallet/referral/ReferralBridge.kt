package org.p2p.wallet.referral

import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
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
import org.p2p.wallet.utils.getClipboardText

private typealias JsResultWrapper = String

/**
 * @see [assets/referral_bridge_provider.js]
 */
class ReferralWebViewBridge(
    webView: WebView,
    private val tokenKeyProvider: TokenKeyProvider,
    private val onShareLinkCalled: (String) -> Unit,
    private val openTerms: (link: String) -> Unit,
    private val navigateToSwap: () -> Unit,
    private val onWebViewLoaded: () -> Unit
) {
    companion object {
        const val JS_BRIDGE_OBJECT_NAME = "AndroidReferralBridge"
        private const val REFERRAL_URL = "https://referral-dapp.key.app/"
    }

    private var referralWebView: WebView? = null

    /**
     * Should be set to false when reloading the page
     */
    private var isJsProviderInjected = false

    private val onPageLoadingFinishedClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            // inject JS provider so WebView can work with our JS interface
            // should be provided again on reload
            if (!isJsProviderInjected) {
                val jsProvider = view.resources.assets.open("referral_bridge_provider.js")
                    .bufferedReader()
                    .readText()

                view.evaluateJavascript(jsProvider, null)
                isJsProviderInjected = true
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (view?.progress == 100) {
                onWebViewLoaded.invoke()
            }
            super.onPageFinished(view, url)
        }
    }

    private val consoleLogger = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            val logMessage = buildString {
                append(consoleMessage.message())
                append(" at ")
                append(consoleMessage.sourceId())
                append(":")
                append(consoleMessage.lineNumber())
            }

            Timber.tag(JS_BRIDGE_OBJECT_NAME).i(logMessage)
            return true
        }
    }

    init {
        referralWebView = webView
        referralWebView?.apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(BridgeInterface(), JS_BRIDGE_OBJECT_NAME)
            webViewClient = onPageLoadingFinishedClient
            webChromeClient = consoleLogger
            loadUrl(REFERRAL_URL)
        }
    }

    fun onFragmentResumed() {
        referralWebView?.onResume()
        referralWebView?.requestFocus()
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

        @JavascriptInterface
        fun showShareDialog(link: String) {
            onShareLinkCalled.invoke(link)
        }

        @JavascriptInterface
        fun openTermsUrl(link: String) {
            openTerms.invoke(link)
        }

        @JavascriptInterface
        fun navigateToSwap() {
            navigateToSwap.invoke()
        }

        @JavascriptInterface
        fun getUserPublicKeyAsync(): JsResultWrapper = makeAsyncCall {
            wrapInJsResult(tokenKeyProvider.publicKey)
        }

        @JavascriptInterface
        fun getClipboardValue(): JsResultWrapper {
            val context = referralWebView?.context ?: return wrapInJsResult(Exception("No Android context found"))
            return wrapInJsResult(context.getClipboardText(trimmed = true).orEmpty())
        }

        @JavascriptInterface
        fun reloadPage() {
            referralWebView?.post {
                isJsProviderInjected = false
                referralWebView?.reload()
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

        // invoke block in IO, return result on Main thread
        private fun makeAsyncCall(block: suspend () -> String): String = runBlocking(Dispatchers.Main.immediate) {
            withContext(Dispatchers.IO) { block.invoke() }
        }
    }
}
