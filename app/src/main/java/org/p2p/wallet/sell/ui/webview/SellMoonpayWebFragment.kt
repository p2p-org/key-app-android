package org.p2p.wallet.sell.ui.webview

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import org.koin.android.ext.android.inject
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.R
import org.p2p.wallet.databinding.FragmentSellMoonpayWebBinding
import org.p2p.wallet.moonpay.model.MoonpayWidgetUrlBuilder
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SellMoonpayWebFragment :
    BaseMvpFragment<SellMoonpayWebContract.View, SellMoonpayWebContract.Presenter>(
        R.layout.fragment_sell_moonpay_web
    ),
    SellMoonpayWebContract.View {

    companion object {
        fun create() = SellMoonpayWebFragment()
    }

    override val presenter: SellMoonpayWebContract.Presenter by inject()
    private val binding: FragmentSellMoonpayWebBinding by viewBinding()
    private val moonpayWidgetUrlBuilder: MoonpayWidgetUrlBuilder by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            webView.settings.javaScriptEnabled = true

            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = WebViewClient()
        }
    }
}
