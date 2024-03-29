package org.p2p.wallet.referral

import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.get
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReferralBinding
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.url.OpenUrlFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
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
        binding.webViewReferral.isInvisible = true

        jsBridge = ReferralWebViewBridge(
            webView = binding.webViewReferral,
            tokenKeyProvider = get(),
            onShareLinkCalled = ::showShareLinkDialog,
            openTerms = ::navigateToTerms,
            onWebViewLoaded = ::onWebViewLoaded,
            navigateToSwap = ::navigateToSwap
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

    private fun navigateToTerms(link: String) {
        val target = OpenUrlFragment.create(
            url = link,
            title = getString(R.string.onboarding_terms_of_use)
        )
        replaceFragment(target)
    }

    private fun navigateToSwap() {
        val target = JupiterSwapFragment.create(source = SwapOpenedFrom.MAIN_SCREEN)
        replaceFragment(target)
    }

    private fun onWebViewLoaded() {
        binding.webViewReferral.isVisible = true
        binding.progressBar.isVisible = false
    }
}
