package org.p2p.wallet.moonpay.ui

import androidx.lifecycle.lifecycleScope
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import org.koin.android.ext.android.inject
import org.p2p.core.utils.Constants
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentMoonpayViewBinding
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.model.MoonpayWidgetUrlBuilder
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import timber.log.Timber
import kotlinx.coroutines.delay

private const val DELAY_IN_MS = 150L

class MoonpayViewFragment : BaseFragment(R.layout.fragment_moonpay_view) {

    companion object {
        private const val ARG_AMOUNT = "EXTRA_AMOUNT"
        private const val ARG_TOKEN_SYMBOL = "ARG_TOKEN_SYMBOL"
        fun create(amount: String, currencyCode: String) = MoonpayViewFragment().withArgs(
            ARG_AMOUNT to amount,
            ARG_TOKEN_SYMBOL to currencyCode,
        )
    }

    private val binding: FragmentMoonpayViewBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val tokenKeyProvider: TokenKeyProvider by inject()
    private val moonpayWidgetUrlBuilder: MoonpayWidgetUrlBuilder by inject()

    private val amount: String by args(ARG_AMOUNT)
    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Buy.EXTERNAL)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            webView.settings.javaScriptEnabled = true

            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = WebViewClient()

            lifecycleScope.launchWhenResumed {
                delay(DELAY_IN_MS)
                val url = moonpayWidgetUrlBuilder.buildBuyWidgetUrl(
                    amount = amount,
                    walletAddress = tokenKeyProvider.publicKey,
                    tokenSymbol = tokenSymbol,
                    currencyCode = Constants.USD_READABLE_SYMBOL.lowercase()
                )
                Timber.tag("Moonpay").i("Loading moonpay with url: $url")
                webView.loadUrl(url)
            }
        }
    }
}
