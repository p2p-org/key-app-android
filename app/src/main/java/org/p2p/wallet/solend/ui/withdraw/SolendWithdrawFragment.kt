package org.p2p.wallet.solend.ui.withdraw

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.glide.GlideManager
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSolendWithdrawBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendTransactionDetailsState
import org.p2p.wallet.solend.model.TransactionDetailsViewData
import org.p2p.wallet.solend.ui.bottomsheet.SelectDepositTokenBottomSheet
import org.p2p.wallet.solend.ui.bottomsheet.TransactionDetailsBottomSheet
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

private const val ARG_WITHDRAW_TOKEN = "ARG_Withdraw_TOKEN"

private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"
private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"

class SolendWithdrawFragment :
    BaseMvpFragment<SolendWithdrawContract.View, SolendWithdrawContract.Presenter>(R.layout.fragment_solend_withdraw),
    SolendWithdrawContract.View {

    companion object {
        fun create(token: SolendDepositToken.Active): SolendWithdrawFragment = SolendWithdrawFragment().withArgs(
            ARG_WITHDRAW_TOKEN to token
        )
    }

    override val presenter: SolendWithdrawContract.Presenter by inject() {
        parametersOf(token)
    }

    private val glideManager: GlideManager by inject()

    private val binding: FragmentSolendWithdrawBinding by viewBinding()

    private val token: SolendDepositToken.Active by args(ARG_WITHDRAW_TOKEN)

    private val depositButtonsAnimation = ChangeBounds().apply {
        duration = 200
        excludeChildren(R.id.viewDoubleInput, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.apply {
                setNavigationOnClickListener {
                    popBackStack()
                }
            }
            sliderDeposit.onSlideCompleteListener = {
                // TODO call to presenter
            }

            viewDoubleInput.setInputLabelText(R.string.solend_deposit_input_label)
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
                result.getParcelable<SolendDepositToken.Active>(KEY_RESULT_TOKEN)?.let {
                    presenter.selectTokenToWithdraw(it)
                }
            }
        }
    }

    override fun showTokenToWithdraw(
        depositToken: SolendDepositToken.Active,
        withChevron: Boolean
    ) = with(binding) {
        glideManager.load(imageViewToken, depositToken.iconUrl.orEmpty())

        amountViewStart.title = "${depositToken.depositAmount.formatToken()} ${depositToken.tokenSymbol}"
        amountViewStart.subtitle = depositToken.tokenName

        val supplyInterestToShow = depositToken.supplyInterest ?: BigDecimal.ZERO
        amountViewEnd.usdAmount = "${supplyInterestToShow.scaleShort()}%"

        val onTokenClick: ((View) -> Unit)?
        val icon: Int?
        val paddingEnd: Int
        if (withChevron) {
            paddingEnd = 0
            onTokenClick = { presenter.onTokenWithdrawClicked() }
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
        setAmountViews(depositToken)
        viewDoubleInput.resetInput()
    }

    private fun setAmountViews(depositToken: SolendDepositToken.Active) = with(binding.viewDoubleInput) {
        val depositAmount = depositToken.depositAmount.orZero()
        val tokenAmount = buildString {
            append(depositAmount.formatToken())
            append(" ")
            append(depositToken.tokenSymbol)
        }
        setOutputLabelText(
            text = getString(R.string.solend_withdraw_output_label_format, tokenAmount),
            amount = depositAmount,
            textMaxAmount = getString(R.string.solend_output_label_using_max)
        )
        setBottomMessageText(R.string.solend_withdraw_bottom_message)
        setAmountHandler(
            maxDepositAmount = depositAmount,
            tokenAmount = tokenAmount
        )
        setInputData(
            inputSymbol = depositToken.tokenSymbol,
            outputSymbol = Constants.USD_READABLE_SYMBOL, // todo: talk to managers about output
            inputRate = depositToken.usdRate.toDouble()
        )
    }

    private fun setAmountHandler(
        maxDepositAmount: BigDecimal,
        tokenAmount: String
    ) = with(binding) {
        viewDoubleInput.amountsHandler = { input, output ->
            val isBiggerThenMax = input > maxDepositAmount
            when {
                input.isZero() && output.isZero() -> setEmptyAmountState()
                isBiggerThenMax -> setBiggerThenMaxAmountState(tokenAmount)
                else -> setValidDepositState(
                    input = input,
                    output = output,
                    tokenAmount = tokenAmount
                )
            }
        }
    }

    private fun setEmptyAmountState() = with(binding) {
        viewDoubleInput.setBottomMessageText(R.string.solend_withdraw_bottom_message)
        buttonAction.apply {
            isEnabled = false
            setText(R.string.main_enter_the_amount)
            setTextColor(getColor(R.color.text_mountain))
        }
        animateButtons(isSliderVisible = false, isInfoButtonVisible = false)
    }

    private fun setBiggerThenMaxAmountState(tokenAmount: String) = with(binding) {
        val maxAmountClickListener = { viewDoubleInput.acceptMaxAmount() }
        viewDoubleInput.setBottomMessageText(R.string.solend_withdraw_bottom_message_with_error)
        buttonAction.apply {
            isEnabled = true
            text = getString(R.string.solend_deposit_button_max_amount, tokenAmount)
            setTextColor(getColor(R.color.text_night))
            setOnClickListener { maxAmountClickListener.invoke() }
        }
        buttonInfo.apply {
            setIconResource(R.drawable.ic_warning_solid)
            iconTint = getColorStateList(R.color.icons_rose)
            backgroundTintList = getColorStateList(R.color.rose_20)
            setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.solend_withdraw_max_amount_error_message)
                    .setNegativeButton(
                        R.string.solend_withdraw_max_amount_error_positive
                    ) { _, _ ->
                        maxAmountClickListener.invoke()
                    }
                    .setPositiveButton(R.string.common_cancel, null)
                    .show()
            }
        }
        animateButtons(isSliderVisible = false, isInfoButtonVisible = true)
    }

    private fun setValidDepositState(
        input: BigDecimal, // TODO PWN-5319 remove if won't be used for slider!
        output: BigDecimal,
        tokenAmount: String
    ) = with(binding) {
        val amount = "$tokenAmount (~$${output.scaleShort()})"
        viewDoubleInput.setBottomMessageText(
            getString(
                R.string.solend_withdraw_bottom_message_with_amount,
                tokenAmount, output.scaleShort()
            )
        )
        buttonInfo.apply {
            setIconResource(R.drawable.ic_info_outline)
            iconTint = getColorStateList(R.color.icons_night)
            backgroundTintList = getColorStateList(R.color.bg_lime)
            setOnClickListener {
                TransactionDetailsBottomSheet.run {
                    show(
                        childFragmentManager,
                        getString(R.string.solend_transaction_details_title),
                        SolendTransactionDetailsState.Withdraw(
                            // TODO PWN-5319 add real data!!
                            TransactionDetailsViewData(
                                amount = amount,
                                transferFee = null,
                                fee = "0.05 USDC (~\$0.5)",
                                total = amount
                            )
                        )
                    )
                }
            }
        }
        buttonAction.apply {
            isEnabled = false
            setText(R.string.main_enter_the_amount)
            setTextColor(getColor(R.color.text_mountain))
        }
        animateButtons(isSliderVisible = true, isInfoButtonVisible = true)
    }

    private fun animateButtons(
        isSliderVisible: Boolean,
        isInfoButtonVisible: Boolean
    ) = with(binding) {
        TransitionManager.beginDelayedTransition(root, depositButtonsAnimation)
        sliderDeposit.isVisible = isSliderVisible
        buttonInfo.isVisible = isInfoButtonVisible
    }

    override fun showTokensToWithdraw(depositTokens: List<SolendDepositToken>) {
        SelectDepositTokenBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.solend_select_token_to_withdraw_title),
            depositTokens = depositTokens,
            requestKey = KEY_REQUEST_TOKEN,
            resultKey = KEY_RESULT_TOKEN
        )
    }
}
