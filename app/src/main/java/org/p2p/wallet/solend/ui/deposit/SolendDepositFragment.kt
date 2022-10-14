package org.p2p.wallet.solend.ui.deposit

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
import org.p2p.wallet.databinding.FragmentSolendDepositBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendTransactionDetailsState
import org.p2p.wallet.solend.model.TransactionDetailsViewData
import org.p2p.wallet.solend.ui.bottomsheet.SelectDepositTokenBottomSheet
import org.p2p.wallet.solend.ui.bottomsheet.TransactionDetailsBottomSheet
import org.p2p.wallet.solend.ui.info.SolendInfoBottomSheet
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
                result.getParcelable<SolendDepositToken>(KEY_RESULT_TOKEN)?.let {
                    presenter.selectTokenToDeposit(it)
                }
            }
        }
    }

    override fun showTokenToDeposit(
        depositToken: SolendDepositToken,
        withChevron: Boolean
    ) = with(binding) {
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
        setAmountViews(depositToken)
    }

    private fun setAmountViews(depositToken: SolendDepositToken) = with(binding.viewDoubleInput) {
        val depositAmount = depositToken.availableTokensForDeposit.orZero()
        val tokenAmount = buildString {
            append(depositAmount.formatToken())
            append(" ")
            append(depositToken.tokenSymbol)
        }
        setOutputLabelText(
            text = getString(R.string.solend_deposit_output_label_format, tokenAmount),
            amount = depositAmount,
            textMaxAmount = getString(R.string.solend_output_label_using_max)
        )
        setBottomMessageText(R.string.solend_deposit_bottom_message_empty)
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
        viewDoubleInput.setBottomMessageText(R.string.solend_deposit_bottom_message_empty)
        buttonAction.apply {
            isEnabled = false
            setText(R.string.main_enter_the_amount)
            setTextColor(getColor(R.color.text_mountain))
        }
        animateButtons(isSliderVisible = false, isInfoButtonVisible = false)
    }

    private fun setBiggerThenMaxAmountState(tokenAmount: String) = with(binding) {
        val maxAmountClickListener = { viewDoubleInput.acceptMaxAmount() }
        viewDoubleInput.setBottomMessageText(R.string.solend_deposit_bottom_message_with_error)
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
                    .setMessage(R.string.solend_deposit_max_amount_error_message)
                    .setNegativeButton(
                        R.string.solend_deposit_max_amount_error_positive
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
                R.string.solend_deposit_bottom_message_with_amount,
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
                        SolendTransactionDetailsState.Deposit(
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
