package org.p2p.wallet.moonpay.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.analytics.ScreenName
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentMoonpayViewBinding
import org.p2p.wallet.home.model.Token
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
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        fun create(amount: String, token: Token.Active) = MoonpayViewFragment().withArgs(
            EXTRA_AMOUNT to amount,
            EXTRA_TOKEN to token
        )
    }

    private val environmentManager: EnvironmentManager by inject()
    private val binding: FragmentMoonpayViewBinding by viewBinding()
    private val analyticsInteractor: AnalyticsInteractor by inject()

    private val amount: String by args(EXTRA_AMOUNT)
    private val token: Token.Active by args(EXTRA_TOKEN)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenName.Buy.EXTERNAL)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            webView.settings.javaScriptEnabled = true

            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = WebViewClient()

            lifecycleScope.launchWhenResumed {
                delay(DELAY_IN_MS)
                val url = environmentManager.getMoonpayUrl(amount, token.publicKey)
                Timber.tag("Moonpay").d("Loading moonpay with url: $url")
                webView.loadUrl(url)
            }
        }
    }
}