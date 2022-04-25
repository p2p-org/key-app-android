package org.p2p.wallet.send.ui.network

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentNetworkSelectionBinding
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.ui.main.KEY_REQUEST_SEND
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

const val EXTRA_NETWORK = "EXTRA_NETWORK"

class NetworkSelectionFragment : BaseFragment(R.layout.fragment_network_selection) {

    companion object {
        fun create(currentNetworkType: NetworkType) =
            NetworkSelectionFragment()
                .withArgs(EXTRA_NETWORK to currentNetworkType)
    }

    private val binding: FragmentNetworkSelectionBinding by viewBinding()

    private val networkType: NetworkType by args(EXTRA_NETWORK)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            solanaView.setOnClickListener {
                setNetworkSelectionResult(NetworkType.SOLANA)
            }

            bitcoinView.setOnClickListener {
                setNetworkSelectionResult(NetworkType.BITCOIN)
            }

            val transactionFee = getString(R.string.send_transaction_fee)
            val zeroFee = getString(R.string.send_zero_usd)
            val commonFee = "$transactionFee:$zeroFee"
            val color = requireContext().getColor(R.color.systemSuccessMain)
            transactionFeeTextView.text = SpanUtils.highlightText(commonFee, zeroFee, color)

            // TODO: remove hardcoded fees
            btcFeeTextView.text = "0.0002 renBTC"
            solanaFeeTextView.text = "0.0002 SOL"

            setNetworkSelected(networkType)
        }
    }

    private fun setNetworkSelectionResult(network: NetworkType) {
        setNetworkSelected(network)
        setFragmentResult(KEY_REQUEST_SEND, bundleOf(EXTRA_NETWORK to network.ordinal))
        val message = getString(R.string.send_network_selected_format, network.stringValue)
        // TODO showSnackBar
    }

    private fun setNetworkSelected(network: NetworkType) {
        binding.solanaRadioButton.isSelected = network == NetworkType.SOLANA
        binding.btcRadioButton.isSelected = network == NetworkType.BITCOIN
    }
}
