package com.p2p.wallet.renBTC.renbtc

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentRenBtcBinding
import com.p2p.wallet.renBTC.model.RenVMStatus
import com.p2p.wallet.renBTC.statuses.ReceivingStatusesFragment
import com.p2p.wallet.utils.addFragment
import com.p2p.wallet.utils.copyToClipBoard
import com.p2p.wallet.utils.cutMiddle
import com.p2p.wallet.utils.setTextBold
import com.p2p.wallet.utils.shareText
import com.p2p.wallet.utils.toast
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class RenBTCFragment :
    BaseMvpFragment<RenBTCContract.View, RenBTCContract.Presenter>(R.layout.fragment_ren_btc),
    RenBTCContract.View {

    companion object {
        fun create() =
            RenBTCFragment()
    }

    override val presenter: RenBTCContract.Presenter by inject()

    private val binding: FragmentRenBtcBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            completeSwitch.setOnCheckedChangeListener { _, isChecked ->
                showButton.isEnabled = isChecked
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

    override fun onStop() {
        super.onStop()
        presenter.cancelTimer()
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    override fun showActiveState(address: String, remaining: String, minTransaction: String?) {
        with(binding) {
            idleState.isVisible = false
            activeState.isVisible = true

            fullAddressTextView.text = address.cutMiddle()
            fullAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(address)
                toast(R.string.common_copied)
            }
            shareImageView.setOnClickListener { requireContext().shareText(address) }

            setAttentionText(minTransaction)
        }
    }

    override fun showLatestStatus(statuses: List<RenVMStatus>) {
        val status = statuses.lastOrNull()
        if (status == null) {
            binding.statusView.setBottomText(R.string.receive_no_statuses_yet)
        } else {
            binding.statusView.setBottomText(getString(status.getStringResId()))
            if (status is RenVMStatus.MinAmountReceived) setAttentionText(status.amount)
        }
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

    override fun showQrLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun showLoading(isLoading: Boolean) {
        binding.completeSwitch.isEnabled = !isLoading
        binding.progressView.isVisible = isLoading
    }

    private fun setAttentionText(minTransaction: String?) {
        binding.attentionImageView.isVisible = false

        val attentionText = buildSpannedString {
            val onlyBitcoin = getString(R.string.receive_only_bitcoin)
            val text = getString(R.string.receive_session_info).setTextBold(onlyBitcoin)
            append(text)

            if (minTransaction.isNullOrEmpty()) return@buildSpannedString
            val minTransactionText = getString(R.string.receive_session_min_transaction, minTransaction)

            append(minTransactionText.setTextBold(minTransaction))
        }

        binding.attentionTextView.text = attentionText
    }
}