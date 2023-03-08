package org.p2p.wallet.renbtc.ui.info

import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.uikit.utils.SpanUtils
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogBtcNetworkInfoBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class RenBtcInfoBottomSheet : NonDraggableBottomSheetDialogFragment() {

    companion object {
        fun show(fm: FragmentManager, requestKey: String, resultKey: String) =
            RenBtcInfoBottomSheet().withArgs(
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey
            ).show(fm, RenBtcInfoBottomSheet::javaClass.name)
    }

    private val binding: DialogBtcNetworkInfoBinding by viewBinding()
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_btc_network_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            progressButton.setOnClickListener {
                setFragmentResult(requestKey, bundleOf(Pair(resultKey, true)))
                dismissAllowingStateLoss()
            }

            val attentionText = buildSpannedString {
                val onlyBitcoin = getString(R.string.receive_only_bitcoin)
                val text = getString(R.string.receive_session_info)
                append(SpanUtils.setTextBold(text, onlyBitcoin))
                append("\n\n")

                val fee = getString(R.string.receive_btc_min_transaction)
                val minTransactionText = getString(R.string.receive_session_min_transaction, fee)
                val btcText = getString(R.string.common_btc)
                append(SpanUtils.setTextBold(minTransactionText, fee, btcText))
                append("\n\n")

                val remainTime = getString(R.string.receive_btc_remain_time)
                val session = getString(R.string.receive_session_timer_info, remainTime)
                append(SpanUtils.setTextBold(session, remainTime))
            }
            infoTextView.text = attentionText
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow
}
