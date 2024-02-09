package org.p2p.wallet.referral

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.get
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReferralBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.viewbinding.viewBinding

class ReferralFragment :
    BaseMvpFragment<ReferralContract.View, ReferralContract.Presenter>(R.layout.fragment_referral),
    ReferralContract.View {

    companion object {
        fun create(): ReferralFragment = ReferralFragment()
    }

    override val presenter: ReferralContract.Presenter = ReferralPresenter()

    private val binding: FragmentReferralBinding by viewBinding()

    private lateinit var jsBridge: ReferralWebViewBridge

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.isVisible = true

        jsBridge = ReferralWebViewBridge(
            webView = binding.webViewReferral,
            tokenKeyProvider = get(),
            onShareLinkCalled = ::showShareLinkDialog,
            onWebViewLoaded = ::onWebViewLoaded
        )
        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        jsBridge.onFragmentResumed()
    }

    override fun onPause() {
        jsBridge.onFragmentPaused()
        super.onPause()
    }

    override fun onDestroyView() {
        jsBridge.onFragmentViewDestroyed()
        super.onDestroyView()
    }

    private fun showShareLinkDialog(link: String) {
        requireContext().shareText(link)
    }

    private fun onWebViewLoaded() {
        binding.progressBar.isVisible = false
    }
}
