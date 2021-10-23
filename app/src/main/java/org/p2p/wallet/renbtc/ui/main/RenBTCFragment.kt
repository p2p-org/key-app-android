package org.p2p.wallet.renbtc.ui.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRenBtcBinding
import org.p2p.wallet.renbtc.model.RenVMStatus
import org.p2p.wallet.renbtc.ui.statuses.ReceivingStatusesFragment
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.setTextBold
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

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
            completeSwitch.setOnCheckedChangeListener { _, isChecked ->
                showButton.isEnabled = isChecked
                val text = if (isChecked) R.string.receive_show_address else R.string.receive_make_transaction_confirm
                showButton.setText(text)
            }

            showButton.setOnClickListener {
                presenter.startNewSession(requireContext())
            }
            statusView.setOnClickListener {
                addFragment(ReceivingStatusesFragment.create())
            }
        }

        presenter.subscribe()
        presenter.checkActiveSession(requireContext())
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
            attentionImageView.isVisible = false
            idleState.isVisible = false
            activeState.isVisible = true

            fullAddressTextView.text = address.cutMiddle()
            fullAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(address)
                toast(R.string.common_copied)
            }
            shareImageView.setOnClickListener { requireContext().shareText(address) }
            viewButton.setOnClickListener {
                val url = getString(R.string.bitcoinExplorer, address)
                showUrlInCustomTabs(url)
            }

            val attentionText = buildSpannedString {
                val onlyBitcoin = getString(R.string.receive_only_bitcoin)
                val text = getString(R.string.receive_session_info).setTextBold(onlyBitcoin)
                append(text)

                val minTransactionText = getString(R.string.receive_session_min_transaction, fee)
                append(minTransactionText.setTextBold(fee))
            }

            attentionTextView.text = attentionText
        }
    }

    override fun showLatestStatus(status: RenVMStatus?) {
        if (status == null) {
            binding.statusView.setBottomText(R.string.receive_no_statuses_yet)
            return
        }

        val text = status.getStringResId(requireContext())
        binding.statusView.setBottomText(text)
    }

    override fun showIdleState() {
        with(binding) {
            idleState.isVisible = true
            activeState.isVisible = false

            attentionImageView.isVisible = true

            val message = getString(R.string.receive_ren_attention_message)
            val openHoursText = getString(R.string.receive_ren_open_hours)
            val riskLosingText = getString(R.string.receive_ren_risk_losing_deposits)
            binding.attentionTextView.text = message.setTextBold(openHoursText, riskLosingText)
        }
    }

    override fun updateTimer(remaining: String) {
        val text = getString(R.string.receive_session_timer_info, remaining).setTextBold(remaining)
        binding.timerTextView.text = text
        binding.timerTextView.isVisible = true
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }
}