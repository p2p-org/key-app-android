package org.p2p.wallet.renbtc.ui.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRenBtcBinding
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.ui.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.main.ui.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.renbtc.ui.transactions.RenTransactionsFragment
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.createBitmap
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding

class RenBTCFragment :
    BaseMvpFragment<RenBTCContract.View, RenBTCContract.Presenter>(R.layout.fragment_ren_btc),
    RenBTCContract.View {

    companion object {
        fun create() = RenBTCFragment()
    }

    override val presenter: RenBTCContract.Presenter by inject()

    private val binding: FragmentRenBtcBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                progressButton.fitMargin { Edge.BottomArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            statusView.setOnClickListener {
                addFragment(RenTransactionsFragment.create())
            }
            networkView.setOnClickListener {
                replaceFragment(ReceiveNetworkTypeFragment.create(NetworkType.BITCOIN))
            }
        }

        presenter.subscribe()
        presenter.checkActiveSession(requireContext())
        presenter.startNewSession(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.cancelTimer()
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    override fun showActiveState(address: String, remaining: String, fee: String) {
        with(binding) {
            fullAddressTextView.text = address.highlightPublicKey(requireContext())
            fullAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(address)
                toast(R.string.common_copied)
            }
            shareButton.setOnClickListener { requireContext().shareText(address) }

            progressButton.setOnClickListener {
                val url = getString(R.string.bitcoinExplorer, address)
                showUrlInCustomTabs(url)
            }
            copyButton.setOnClickListener {
                requireContext().copyToClipBoard(address)
                toast(R.string.common_copied)
            }
            saveButton.setOnClickListener {
                val bitmap = qrView.createBitmap()
                // TODO ask which name use here ?
                presenter.saveQr("", bitmap)
            }
            val infoText = getString(R.string.receive_session_info)
            val onlyBitcoin = getString(R.string.receive_only_bitcoin)
            sessionInfoTextView.text = SpanUtils.setTextBold(infoText, onlyBitcoin)

            val amountText = getString(R.string.receive_session_min_transaction, fee)
            amountInfoTextView.text = SpanUtils.setTextBold(amountText, fee)

            setFragmentResultListener(ReceiveNetworkTypeFragment.REQUEST_KEY) { _, bundle ->
                val type = bundle.get(ReceiveNetworkTypeFragment.BUNDLE_NETWORK_KEY) as NetworkType
                if (type == NetworkType.SOLANA) {
                    popAndReplaceFragment(ReceiveSolanaFragment.create(null))
                }
            }
        }
    }

    override fun updateTimer(remaining: String) {
        val text = getString(R.string.receive_session_timer_info, remaining)
        binding.timerTextView.text = SpanUtils.setTextBold(text, remaining)
        binding.timerTextView.isVisible = true
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showToastMessage(resId: Int) {
        toast(resId)
    }

    override fun navigateToSolana() {
        popAndReplaceFragment(ReceiveSolanaFragment.create(null))
    }
}