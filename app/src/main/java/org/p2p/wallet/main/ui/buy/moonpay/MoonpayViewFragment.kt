package org.p2p.wallet.main.ui.buy.moonpay

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentMoonpayViewBinding
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import timber.log.Timber

private const val DELAY_IN_MS = 150L

class MoonpayViewFragment : BaseFragment(R.layout.fragment_moonpay_view) {

    companion object {
        private const val EXTRA_AMOUNT = "EXTRA_AMOUNT"
        fun create(amount: String) = MoonpayViewFragment().withArgs(
            EXTRA_AMOUNT to amount
        )
    }

    private val environmentManager: EnvironmentManager by inject()

    private val binding: FragmentMoonpayViewBinding by viewBinding()

    private val amount: String by args(EXTRA_AMOUNT)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            webView.settings.javaScriptEnabled = true

            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progressBar.isInvisible = newProgress == 100
                }
            }

            webView.webViewClient = WebViewClient()

            lifecycleScope.launchWhenResumed {
                delay(DELAY_IN_MS)
                val url = environmentManager.getMoonpayUrl(amount)
                Timber.tag("Moonpay").d("Loading moonpay with url: $url")
                webView.loadUrl(url)
            }
        }
    }
}