package com.p2p.wowlet.fragment.blockchainexplorer.view

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentBlockChainExplorerBinding
import com.p2p.wowlet.fragment.blockchainexplorer.viewmodel.BlockChainExplorerViewModel
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.withArgs
import org.koin.androidx.viewmodel.ext.android.viewModel

class BlockChainExplorerFragment :
    FragmentBaseMVVM<BlockChainExplorerViewModel, FragmentBlockChainExplorerBinding>() {

    override val viewModel: BlockChainExplorerViewModel by viewModel()
    override val binding: FragmentBlockChainExplorerBinding by dataBinding(R.layout.fragment_block_chain_explorer)

    companion object {
        const val BLOCK_CHAIN_URL = "block_chain_url"

        fun createScreen(url: String) =
            BlockChainExplorerFragment().withArgs(
                BLOCK_CHAIN_URL to url
            )
    }

    private var url: String? = null

    override fun initData() {
        arguments?.let {
            url = it.getString(BLOCK_CHAIN_URL)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
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

    override fun processViewCommand(command: ViewCommand) {
        super.processViewCommand(command)
        when (command) {
            is Command.NavigateUpViewCommand -> popBackStack()
        }
    }
}