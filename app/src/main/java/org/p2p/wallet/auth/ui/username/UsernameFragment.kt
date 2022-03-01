package org.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentUsernameBinding
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.list.TokenListFragment
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding

class UsernameFragment :
    BaseMvpFragment<UsernameContract.View, UsernameContract.Presenter>(R.layout.fragment_username),
    UsernameContract.View {

    companion object {
        fun create() = UsernameFragment()
    }

    override val presenter: UsernameContract.Presenter by inject()

    private val binding: FragmentUsernameBinding by viewBinding()
    private val receiveAnalytics: ReceiveAnalytics by inject()
    private val analyticsInteractor: AnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                bottomSheetView.fitMargin { Edge.BottomArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            receiveCardView.setOnSaveQrClickListener { name, qrImage ->
                presenter.saveQr(binding.receiveCardView.getQrName())
                receiveAnalytics.logReceiveQrSaved(analyticsInteractor.getPreviousScreenName())
            }
            receiveCardView.setSelectNetworkVisibility(isVisible = false)
            receiveCardView.setFaqVisibility(isVisible = false)
            progressButton.setOnClickListener {
                replaceFragment(TokenListFragment.create())
            }
        }
        presenter.loadData()
    }

    override fun showUsername(username: Username) {
        val fullUsername = username.getFullUsername(requireContext())
        binding.receiveCardView.setQrName(fullUsername)

        binding.receiveCardView.setOnCopyQrClickListener {
            receiveAnalytics.logReceiveAddressCopied(analyticsInteractor.getPreviousScreenName())
        }

        binding.receiveCardView.setOnShareQrClickListener {
            requireContext().shareText(fullUsername)
            receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
        }
    }

    override fun renderQr(qrBitmap: Bitmap) {
        binding.receiveCardView.setQrBitmap(qrBitmap)
        binding.receiveCardView.showQrLoading(false)
    }

    override fun showAddress(address: String) {
        binding.receiveCardView.setQrValue(address.highlightPublicKey(requireContext()))
    }

    override fun showToastMessage(messageRes: Int) {
        toast(messageRes)
    }
}