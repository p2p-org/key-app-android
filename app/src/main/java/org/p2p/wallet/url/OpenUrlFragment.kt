package org.p2p.wallet.url

import androidx.core.view.isVisible
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.FragmentOpenUrlBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class OpenUrlFragment : BaseMvpFragment<MvpView, NoOpPresenter<MvpView>>(
    R.layout.fragment_open_url
) {

    companion object {
        private const val ARG_URL = "ARG_URL"
        private const val ARG_BACK_ENABLED = "ARG_BACK_ENABLED"
        private const val ARG_TITLE = "ARG_TITLE"
        fun create(
            url: String,
            title: String? = null,
            isBackEnabled: Boolean = true,
        ): OpenUrlFragment {
            return OpenUrlFragment()
                .withArgs(
                    ARG_URL to url,
                    ARG_BACK_ENABLED to isBackEnabled,
                    ARG_TITLE to title
                )
        }
    }

    override val presenter = NoOpPresenter<MvpView>()

    private val binding: FragmentOpenUrlBinding by viewBinding()

    private val webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            binding.progressBar.isVisible = true
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (view?.progress == 100) {
                binding.progressBar.isVisible = false
            }
            super.onPageFinished(view, url)
        }
    }

    private val toolbarTitle: String? by args(ARG_TITLE)
    private val isBackEnabled: Boolean by args(ARG_BACK_ENABLED, true)
    private val url: String by args(ARG_URL)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isBackEnabled) {
            binding.toolbar.setNavigationOnClickListener { popBackStack() }
        } else {
            binding.toolbar.navigationIcon = null
        }
        if (toolbarTitle != null) {
            binding.toolbar.title = toolbarTitle
        }

        binding.webViewUrl.webViewClient = webViewClient
        binding.webViewUrl.loadUrl(url)
    }

    override fun onResume() {
        super.onResume()
        binding.webViewUrl.onResume()
        binding.webViewUrl.requestFocus()
    }

    override fun onPause() {
        binding.webViewUrl.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        binding.webViewUrl.destroy()
        super.onDestroyView()
    }
}
