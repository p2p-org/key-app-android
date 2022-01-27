package org.p2p.wallet.renbtc.ui.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRenBtcBinding
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.ui.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.main.ui.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.renbtc.ui.transactions.RenTransactionsFragment
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.createBitmap
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popAndReplaceFragment
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
    private val callback: (NetworkType) -> Unit = { type ->
        if (type == NetworkType.SOLANA) {
            popAndReplaceFragment(ReceiveSolanaFragment.create(null))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                progressButton.fitMargin { Edge.BottomArc }
            }
            statusView.setOnClickListener {
                addFragment(RenTransactionsFragment.create())
            }
            networkView.setOnClickListener {
                replaceFragment(ReceiveNetworkTypeFragment.create(NetworkType.BITCOIN, callback))
            }
            saveButton.setOnClickListener {
                val bitmap = qrView.createBitmap()
                presenter.saveQr("", bitmap)
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
            fullAddressTextView.text = address
            fullAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(address)
                toast(R.string.common_copied)
            }
            shareButton.setOnClickListener { requireContext().shareText(address) }
            progressButton.setOnClickListener {
                val url = getString(R.string.bitcoinExplorer, address)
                showUrlInCustomTabs(url)
            }
            val infoText = getString(R.string.receive_session_info)
            val onlyBitcoin = getString(R.string.receive_only_bitcoin)
            sessionInfoTextView.text = SpanUtils.setTextBold(infoText, onlyBitcoin)

            val amountText = getString(R.string.receive_session_min_transaction, fee)
            amountInfoTextView.text = SpanUtils.setTextBold(amountText, fee)
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
}