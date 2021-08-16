package com.p2p.wallet.main.ui.buy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isInvisible
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentBuyBinding
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class BuyFragment :
    BaseMvpFragment<BuyContract.View, BuyContract.Presenter>(R.layout.fragment_buy),
    BuyContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        fun create(token: Token?) = BuyFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token? by args(EXTRA_TOKEN)

    override val presenter: BuyContract.Presenter by inject {
        parametersOf(token)
    }

    private val binding: FragmentBuyBinding by viewBinding()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            webView.settings.javaScriptEnabled = true

            binding.webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.isInvisible = newProgress == 100
                }
            }

            binding.webView.webViewClient = WebViewClient()
        }

        presenter.loadData()
    }

    override fun openWebView(url: String) {
        binding.webView.loadUrl(url)
    }
}