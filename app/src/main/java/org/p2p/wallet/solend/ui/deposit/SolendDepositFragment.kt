package org.p2p.wallet.solend.ui.deposit

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.glide.GlideManager
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSolendDepositBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.ui.bottomsheet.SelectDepositTokenBottomSheet
import org.p2p.wallet.solend.ui.info.SolendInfoBottomSheet
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

private const val ARG_DEPOSIT_TOKEN = "ARG_DEPOSIT_TOKEN"

private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"
private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"

class SolendDepositFragment :
    BaseMvpFragment<SolendDepositContract.View, SolendDepositContract.Presenter>(
        R.layout.fragment_solend_deposit
    ),
    SolendDepositContract.View {

    companion object {
        fun create(deposit: SolendDepositToken) = SolendDepositFragment().withArgs(
            ARG_DEPOSIT_TOKEN to deposit
        )
    }

    override val presenter: SolendDepositContract.Presenter by inject {
        parametersOf(deposit)
    }

    private val glideManager: GlideManager by inject()

    private val binding: FragmentSolendDepositBinding by viewBinding()

    private val deposit: SolendDepositToken by args(ARG_DEPOSIT_TOKEN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.apply {
                setNavigationOnClickListener {
                    popBackStack()
                }
                setOnMenuItemClickListener {
                    if (it.itemId == R.id.itemHelp) {
                        SolendInfoBottomSheet.show(
                            fm = childFragmentManager,
                            getString(R.string.solend_info_title)
                        )
                        true
                    } else {
                        false
                    }
                }
            }
        }
        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_TOKEN,
            viewLifecycleOwner,
            ::onFragmentResult
        )
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            KEY_REQUEST_TOKEN -> {
                result.getParcelable<SolendDepositToken>(KEY_RESULT_TOKEN)?.let {
                    presenter.selectTokenToDeposit(it)
                }
            }
        }
    }

    override fun showTokenToDeposit(depositToken: SolendDepositToken, withChevron: Boolean) = with(binding) {
        glideManager.load(imageViewToken, depositToken.iconUrl.orEmpty())

        if (depositToken is SolendDepositToken.Active) {
            amountViewStart.title = "${depositToken.depositAmount.formatToken()} ${depositToken.tokenSymbol}"
        } else {
            amountViewStart.title = depositToken.tokenSymbol
        }
        amountViewStart.subtitle = depositToken.tokenName

        val supplyInterestToShow = depositToken.supplyInterest ?: BigDecimal.ZERO
        amountViewEnd.usdAmount = "${supplyInterestToShow.scaleShort()}%"

        val onTokenClick: ((View) -> Unit)?
        val icon: Int?
        val paddingEnd: Int
        if (withChevron) {
            paddingEnd = 0
            onTokenClick = { presenter.onTokenDepositClicked() }
            icon = R.drawable.ic_chevron_right
        } else {
            paddingEnd = 32.toPx()
            onTokenClick = null
            icon = null
        }
        amountViewEnd.setPadding(0, 0, paddingEnd, 0)
        amountViewEnd.icon = icon

        imageViewToken.setOnClickListener(onTokenClick)
        amountViewStart.setOnClickListener(onTokenClick)
        amountViewEnd.setOnClickListener(onTokenClick)
    }

    override fun showTokensToDeposit(depositTokens: List<SolendDepositToken>) {
        SelectDepositTokenBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.solend_select_token_to_deposit_title),
            depositTokens = depositTokens,
            requestKey = KEY_REQUEST_TOKEN,
            resultKey = KEY_RESULT_TOKEN
        )
    }
}
