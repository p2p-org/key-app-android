package com.p2p.wallet.main.ui.receive.renbtc

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentRenBtcBinding
import com.p2p.wallet.utils.copyToClipBoard
import com.p2p.wallet.utils.cutMiddle
import com.p2p.wallet.utils.setTextBold
import com.p2p.wallet.utils.shareText
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

            receiveCoinView.setOnClickListener {
            }

            showButton.setOnClickListener { presenter.showAddress() }
        }

        presenter.loadSessionIfExists()
    }

    override fun onStop() {
        super.onStop()
        presenter.cancelTimer()
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    override fun showActiveState(address: String, remaining: String) {
        with(binding) {
            idleState.isVisible = false
            activeState.isVisible = true

            setAttentionText(remaining)

            fullAddressTextView.text = address.cutMiddle()
            fullAddressTextView.setOnClickListener { requireContext().copyToClipBoard(address) }
            shareImageView.setOnClickListener { requireContext().shareText(address) }
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
            binding.attentionTextView.text = message/*.setTextBold(openHoursText, riskLosingText)*/
        }
    }

    override fun updateTimer(remaining: String) {
        val text = getString(R.string.receive_session_timer_info, remaining)

        val onlyBitcoin = getString(R.string.receive_only_bitcoin)
        val minTransaction = getString(R.string.receive_btc_min_transaction)
        val boldText = text/*.setTextBold(onlyBitcoin, minTransaction, remaining)*/

        binding.attentionTextView.text = boldText
    }

    override fun showQrLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun showLoading(isLoading: Boolean) {
        binding.completeSwitch.isEnabled = !isLoading
        binding.progressView.isVisible = isLoading
    }

    private fun setAttentionText(remainingTime: String) {
        binding.attentionImageView.isVisible = false
        val text = getString(R.string.receive_session_timer_info, remainingTime)
        binding.attentionTextView.text = text
    }
}