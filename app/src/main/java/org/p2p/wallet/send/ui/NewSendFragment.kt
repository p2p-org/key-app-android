package org.p2p.wallet.send.ui

import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.send.ui.details.NewSendDetailsBottomSheet
import org.p2p.wallet.send.ui.dialogs.SendFreeTransactionsDetailsBottomSheet
import org.p2p.wallet.send.ui.dialogs.SendFreeTransactionsDetailsBottomSheet.OpenedFrom
import org.p2p.wallet.send.ui.search.NewSearchFragment
import org.p2p.wallet.send.ui.stub.SendNoAccountFragment
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.progresshandler.SendSwapTransactionProgressHandler
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getParcelableArrayListCompat
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val ARG_RECIPIENT = "ARG_RECIPIENT"
private const val ARG_INITIAL_TOKEN = "ARG_INITIAL_TOKEN"
private const val ARG_INPUT_AMOUNT = "ARG_INPUT_AMOUNT"
private const val ARG_OPENED_FROM_FLOW = "ARG_OPENED_FROM_FLOW"

private const val KEY_RESULT_FEE = "KEY_RESULT_FEE"
private const val KEY_RESULT_FEE_PAYER_TOKENS = "KEY_RESULT_FEE_PAYER_TOKENS"
private const val KEY_RESULT_NEW_FEE_PAYER = "KEY_RESULT_APPROXIMATE_FEE_USD"
private const val KEY_RESULT_TOKEN_TO_SEND = "KEY_RESULT_TOKEN_TO_SEND"
private const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

class NewSendFragment :
    BaseMvpFragment<NewSendContract.View, NewSendContract.Presenter>(R.layout.fragment_send_new),
    NewSendContract.View {

    companion object {
        fun create(
            recipient: SearchResult,
            initialToken: Token.Active? = null,
            inputAmount: BigDecimal? = null,
            openedFromFlow: SendOpenedFrom = SendOpenedFrom.MAIN_FLOW,
        ): NewSendFragment = NewSendFragment()
            .withArgs(
                ARG_RECIPIENT to recipient,
                ARG_INITIAL_TOKEN to initialToken,
                ARG_INPUT_AMOUNT to inputAmount,
                ARG_OPENED_FROM_FLOW to openedFromFlow,
            )
    }

    private val recipient: SearchResult by args(ARG_RECIPIENT)
    private val initialToken: Token.Active? by args(ARG_INITIAL_TOKEN)
    private val inputAmount: BigDecimal? by args(ARG_INPUT_AMOUNT)
    private val openedFromFlow: SendOpenedFrom by args(ARG_OPENED_FROM_FLOW)

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: NewSendContract.Presenter by inject { parametersOf(recipient, openedFromFlow) }

    private var listener: RootListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setInitialData(initialToken, inputAmount)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupToolbar()
        binding.widgetSendDetails.apply {
            tokenClickListener = presenter::onTokenClicked
            amountListener = presenter::updateInputAmount
            maxButtonClickListener = presenter::onMaxButtonClicked
            switchListener = presenter::switchCurrencyMode
            feeButtonClickListener = presenter::onFeeInfoClicked
            if (inputAmount == null) {
                focusAndShowKeyboard()
            }
        }
        binding.sliderSend.onSlideCompleteListener = { presenter.checkInternetConnection() }
        binding.sliderSend.onSlideCollapseCompleted = { presenter.send() }

        binding.textViewDebug.isVisible = BuildConfig.DEBUG

        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result -> handleSupportFragmentResult(result) }

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(KEY_RESULT_FEE) && result.containsKey(KEY_RESULT_FEE_PAYER_TOKENS) -> {
                    val fee = result.getParcelableCompat<SendSolanaFee>(KEY_RESULT_FEE)
                    val feePayerTokens = result.getParcelableArrayListCompat<Token.Active>(KEY_RESULT_FEE_PAYER_TOKENS)
                    if (fee == null) return@setFragmentResultListener
                    showAccountCreationFeeInfo(fee, feePayerTokens)
                }
            }
        }
    }

    private fun handleSupportFragmentResult(result: Bundle) {
        when {
            // will be more!
            result.containsKey(KEY_RESULT_TOKEN_TO_SEND) -> {
                val token = result.getParcelableCompat<Token.Active>(KEY_RESULT_TOKEN_TO_SEND)!!
                presenter.updateToken(token)
            }
            result.containsKey(KEY_RESULT_NEW_FEE_PAYER) -> {
                val newFeePayer = result.getParcelableCompat<Token.Active>(KEY_RESULT_NEW_FEE_PAYER)!!
                presenter.updateFeePayerToken(newFeePayer)
            }
        }
    }

    override fun showTransactionDetails(
        sendFeeTotal: SendFeeTotal,
        inputAmount: BigInteger,
        useMax: Boolean,
    ) {
        NewSendDetailsBottomSheet.show(
            fm = childFragmentManager,
            totalFee = sendFeeTotal,
            inputAmount = inputAmount,
            useMax = useMax,
            requestKey = KEY_REQUEST_SEND,
            feeResultKey = KEY_RESULT_FEE,
            feePayerTokensResultKey = KEY_RESULT_FEE_PAYER_TOKENS
        )
    }

    override fun showFreeTransactionsInfo() {
        SendFreeTransactionsDetailsBottomSheet.show(childFragmentManager, openedFrom = OpenedFrom.SEND)
    }

    override fun updateInputValue(textValue: String, forced: Boolean) {
        binding.widgetSendDetails.setInput(textValue, forced)
    }

    override fun updateInputFraction(newInputFractionLength: Int) {
        binding.widgetSendDetails.updateFractionLength(newInputFractionLength)
    }

    override fun showToken(token: Token.Active) {
        binding.widgetSendDetails.setToken(token)
    }

    override fun setMaxButtonVisible(isVisible: Boolean) {
        binding.widgetSendDetails.setMaxButtonVisible(isVisible)
    }

    override fun setInputEnabled(isEnabled: Boolean) {
        binding.widgetSendDetails.setInputEnabled(isEnabled)
    }

    override fun setBottomButtonText(text: TextContainer?) {
        binding.buttonBottom withTextOrGone text?.getString(requireContext())
    }

    override fun setSliderText(text: String?) {
        if (text.isNullOrEmpty()) {
            binding.sliderSend.isVisible = !text.isNullOrEmpty()
        } else {
            binding.sliderSend.isVisible = true
            binding.sliderSend.setActionText(text)
        }
    }

    override fun disableSwitchAmounts() {
        binding.widgetSendDetails.disableSwitchAmounts()
    }

    override fun enableSwitchAmounts() {
        binding.widgetSendDetails.enableFiat()
    }

    override fun disableInputs() {
        binding.widgetSendDetails.disableInputs()
    }

    override fun showAroundValue(value: String) {
        binding.widgetSendDetails.setAroundValue(value)
    }

    override fun setTokenContainerEnabled(isEnabled: Boolean) {
        binding.widgetSendDetails.setTokenContainerEnabled(isEnabled = isEnabled)
    }

    override fun showFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.showFeeLoading(isLoading)
    }

    override fun showDelayedFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.showDelayedFeeViewLoading(isLoading)
    }

    override fun showFeeViewVisible(isVisible: Boolean) {
        binding.widgetSendDetails.showFeeVisible(isVisible = isVisible)
    }

    override fun setFeeLabel(text: String) {
        binding.widgetSendDetails.setFeeLabel(text)
    }

    override fun setFeeLabelRes(textRes: Int) {
        this.setFeeLabel(getString(textRes))
    }

    override fun showBottomFeeValue(fee: TextViewCellModel) {
        binding.widgetSendDetails.showBottomFeeValue(fee)
    }

    override fun setFeeColor(@ColorRes colorRes: Int) {
        binding.widgetSendDetails.setBottomFeeColor(colorRes)
    }

    override fun setTotalValue(text: String) {
        binding.widgetSendDetails.setTotalValue(text)
    }

    override fun setSwitchLabel(symbol: String) {
        binding.widgetSendDetails.setSwitchLabel(getString(R.string.send_switch_to_token, symbol))
    }

    override fun setMainAmountLabel(symbol: String) {
        binding.widgetSendDetails.setMainAmountLabel(symbol)
    }

    override fun setInputColor(@ColorRes colorRes: Int) {
        binding.widgetSendDetails.setInputTextColor(colorRes)
    }

    override fun showDebugInfo(text: CharSequence) {
        binding.textViewDebug.text = text
    }

    override fun showTokenSelection(selectedToken: Token.Active?) {
        addFragment(
            target = NewSelectTokenFragment.create(
                selectedToken = selectedToken,
                requestKey = KEY_REQUEST_SEND,
                resultKey = KEY_RESULT_TOKEN_TO_SEND
            ),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showProgressDialog(internalTransactionId: String, data: NewShowProgress) {
        listener?.showTransactionProgress(internalTransactionId, data, SendSwapTransactionProgressHandler.QUALIFIER)
        when (openedFromFlow) {
            SendOpenedFrom.SELL_FLOW -> popBackStackTo(target = MainContainerFragment::class, inclusive = false)
            SendOpenedFrom.MAIN_FLOW -> popBackStackTo(target = NewSearchFragment::class, inclusive = true)
        }
    }

    override fun showSliderCompleteAnimation() {
        binding.sliderSend.showCompleteAnimation()
    }

    override fun restoreSlider() {
        binding.sliderSend.restoreSlider()
    }

    private fun showAccountCreationFeeInfo(
        fee: SendSolanaFee,
        alternativeFeePayerTokens: List<Token.Active>
    ) {
        val target = SendNoAccountFragment.create(
            tokenSymbol = fee.feePayerSymbol,
            approximateFeeUsd = fee.getApproxAccountCreationFeeUsd(withBraces = false).orEmpty(),
            alternativeFeePayerTokens = alternativeFeePayerTokens,
            requestKey = KEY_REQUEST_SEND,
            resultKey = KEY_RESULT_NEW_FEE_PAYER
        )
        replaceFragment(target)
    }

    private fun UiKitToolbar.setupToolbar() {
        val toolbarTitle = when (val recipient = recipient) {
            is SearchResult.UsernameFound -> recipient.formattedUsername
            else -> recipient.formattedAddress
        }
        title = toolbarTitle
        setNavigationOnClickListener { popBackStack() }
    }
}
