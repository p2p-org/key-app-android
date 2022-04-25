package org.p2p.wallet.renbtc.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogBtcBuyInfoBinding
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class RenBtcBuyBottomSheet : NonDraggableBottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_PRICE_IN_SOL = "EXTRA_PRICE_IN_SOL"
        private const val EXTRA_PRICE_IN_USD = "EXTRA_PRICE_IN_USD"
        fun show(
            fm: FragmentManager,
            priceInSol: BigDecimal,
            priceInUsd: BigDecimal?,
            requestKey: String,
            resultKey: String,
        ) = RenBtcBuyBottomSheet().withArgs(
            EXTRA_PRICE_IN_SOL to priceInSol,
            EXTRA_PRICE_IN_USD to priceInUsd,
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        ).show(fm, RenBtcBuyBottomSheet::javaClass.name)
    }

    private val binding: DialogBtcBuyInfoBinding by viewBinding()
    private val priceInSol: BigDecimal by args(EXTRA_PRICE_IN_SOL)
    private val priceInUsd: BigDecimal? by args(EXTRA_PRICE_IN_USD)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_btc_buy_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val feeUsd = if (priceInUsd != null) "~$$priceInUsd" else getString(R.string.common_not_available)
            topTextView.text = getString(R.string.send_account_creation_fee_format, feeUsd)
            amountTextView.text = getString(R.string.receive_amount_in_sol, priceInSol.toString())
            with(progressButton) {
                setActionText(getString(R.string.receive_pay_and_countinue, priceInSol.toString()))
                setOnClickListener { onBuySelected() }
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

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    private fun onBuySelected() {
        setFragmentResult(requestKey, bundleOf(Pair(resultKey, true)))
        dismissAllowingStateLoss()
    }
}
