package com.p2p.wowlet.fragment.blockchainexplorer.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentBlockChainExplorerBinding
import com.p2p.wowlet.utils.viewbinding.viewBinding
import com.p2p.wowlet.utils.withArgs

class BlockChainExplorerFragment : BaseFragment(R.layout.fragment_block_chain_explorer) {

    private val binding: FragmentBlockChainExplorerBinding by viewBinding()

    companion object {
        const val BLOCK_CHAIN_URL = "block_chain_url"

        fun createScreen(url: String) =
            BlockChainExplorerFragment().withArgs(
                BLOCK_CHAIN_URL to url
            )
    }

    private var url: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        arguments?.let {
            url = it.getString(BLOCK_CHAIN_URL)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        // binding.webView.settings.javaScriptEnabled = true;
        // указываем страницу загрузки
        url?.let {
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.settings.domStorageEnabled = true

            binding.webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView,
                    errorCode: Int,
                    description: String,
                    failingUrl: String
                ) {
                    Toast.makeText(activity, description, Toast.LENGTH_SHORT).show()
                }
            }

            binding.webView.loadUrl(it)

        }
    }
}