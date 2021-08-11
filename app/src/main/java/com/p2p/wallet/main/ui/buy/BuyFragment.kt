package com.p2p.wallet.main.ui.buy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.view.isInvisible
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentBuyBinding
import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

class BuyFragment : BaseFragment(R.layout.fragment_buy) {

    companion object {

        fun create() = BuyFragment()
    }

    private val environmentManager: EnvironmentManager by inject()

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

            val transakUrl = environmentManager.getTransakUrl()
            Timber.d("### url $transakUrl")
            webView.loadUrl(transakUrl)
        }
    }
}