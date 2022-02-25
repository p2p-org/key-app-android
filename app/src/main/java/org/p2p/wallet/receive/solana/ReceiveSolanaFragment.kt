package org.p2p.wallet.receive.solana

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveSolanaBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.analytics.ScreenName
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.receive.list.TokenListFragment
import org.p2p.wallet.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.renbtc.ui.main.RenBTCFragment
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.toast

class ReceiveSolanaFragment :
    BaseMvpFragment<ReceiveSolanaContract.View, ReceiveSolanaContract.Presenter>(R.layout.fragment_receive_solana),
    ReceiveSolanaContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        private const val REQUEST_KEY = "REQUEST_KEY"
        private const val BUNDLE_KEY_NETWORK_TYPE = "BUNDLE_KEY_NETWORK_TYPE"
        fun create(token: Token?) = ReceiveSolanaFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token? by args(EXTRA_TOKEN)
    override val presenter: ReceiveSolanaContract.Presenter by inject {
        parametersOf(token)
    }
    private val binding: FragmentReceiveSolanaBinding by viewBinding()
    private val analyticsInteractor: AnalyticsInteractor by inject()
    private val receiveAnalytics: ReceiveAnalytics by inject()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenName.Receive.SOLANA)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                progressButton.fitMargin { Edge.BottomArc }
            }
            networkView.setOnClickListener {
                presenter.onNetworkClicked()
            }
            faqTextView.setOnClickListener {
                analyticsInteractor.logScreenOpenEvent(ScreenName.Receive.LIST)
                replaceFragment(TokenListFragment.create())
            }
            qrView.onShareClickListener = {
                receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
            }
            qrView.onCopyClickListener = {
                receiveAnalytics.logReceiveAddressCopied(analyticsInteractor.getPreviousScreenName())
            }
            qrView.onSaveClickListener = { name, bitmap ->
                presenter.saveQr(name, bitmap)
            }
            setFragmentResultListener(REQUEST_KEY) { _, bundle ->
                val type = bundle.get(BUNDLE_KEY_NETWORK_TYPE) as NetworkType
                if (type == NetworkType.BITCOIN) {
                    popAndReplaceFragment(RenBTCFragment.create())
                }
            }
        }
        presenter.loadData()
    }

    override fun showUserData(userPublicKey: String, username: Username?) {
        with(binding) {
            qrView.setValue(userPublicKey.highlightPublicKey(requireContext()))
            username?.getFullUsername(requireContext())?.let {
                qrView.setName(it)
            }
            progressButton.setOnClickListener {
                val url = getString(R.string.solanaWalletExplorer, userPublicKey)
                showUrlInCustomTabs(url)
            }
        }
    }

    override fun showReceiveToken(token: Token.Active) {
        binding.progressButton.setOnClickListener {
            presenter.onBrowserClicked(token.publicKey)
            val url = getString(R.string.solanaWalletExplorer, token.publicKey)
            showUrlInCustomTabs(url)
        }
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        if (qrBitmap != null) {
            binding.qrView.setImage(qrBitmap)
        }
    }

    override fun showQrLoading(isLoading: Boolean) {
        binding.qrView.showLoading(isLoading)
        binding.progressView.isVisible = isLoading
    }

    override fun showToastMessage(resId: Int) {
        toast(resId)
    }

    override fun showNetwork() {
        replaceFragment(ReceiveNetworkTypeFragment.create(NetworkType.SOLANA, REQUEST_KEY, BUNDLE_KEY_NETWORK_TYPE))
    }

    override fun showBrowser(url: String) {
        showUrlInCustomTabs(url)
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }
}