package org.p2p.wallet.solend.ui.withdraw

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSolendWithdrawBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.asUsd
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_DEPOSIT = "ARG_DEPOSIT"

class SolendWithdrawFragment :
    BaseMvpFragment<SolendWithdrawContract.View, SolendWithdrawContract.Presenter>(R.layout.fragment_solend_withdraw),
    SolendWithdrawContract.View {

    companion object {
        fun create(token: SolendDepositToken.Active): SolendWithdrawFragment = SolendWithdrawFragment().withArgs(
            ARG_DEPOSIT to token
        )
    }

    override val presenter: SolendWithdrawContract.Presenter by inject()

    private val glideManager: GlideManager by inject()

    private val binding: FragmentSolendWithdrawBinding by viewBinding()

    private val token: SolendDepositToken.Active by args(ARG_DEPOSIT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            glideManager.load(imageViewToken, token.iconUrl)
            val tokenAmount = buildString {
                append(token.depositAmount.formatToken())
                append(" ")
                append(token.tokenSymbol)
            }
            textViewTokenAmount.text = tokenAmount

            textViewApy.text =
                getString(R.string.solend_withdraw_yielding_apy_format, token.supplyInterest?.scaleShort())
            textViewAmountUsd.text = token.usdAmount.asUsd()

            viewDoubleInput.setInputLabelText(R.string.solend_withdraw_input_label)
            viewDoubleInput.setOutputLabelText(
                text = getString(R.string.solend_withdraw_output_label_format, tokenAmount),
                amount = token.depositAmount
            )
            viewDoubleInput.setBottomMessageText(R.string.solend_withdraw_bottom_message)
            viewDoubleInput.setInputData(
                inputSymbol = token.tokenSymbol,
                outputSymbol = USD_READABLE_SYMBOL, // todo: talk to managers about output
                inputRate = token.usdRate.toDouble()
            )
        }
    }
}
