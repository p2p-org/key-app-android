package org.p2p.wallet.newsend.ui

import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.newsend.ui.details.NewSendDetailsBottomSheet
import org.p2p.wallet.newsend.ui.dialogs.FreeTransactionsDetailsBottomSheet
import org.p2p.wallet.newsend.ui.search.NewSearchFragment
import org.p2p.wallet.newsend.ui.stub.SendNoAccountFragment
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone
import java.math.BigDecimal

private const val ARG_RECIPIENT = "ARG_RECIPIENT"
private const val ARG_INITIAL_TOKEN = "ARG_INITIAL_TOKEN"
private const val ARG_INPUT_AMOUNT = "ARG_INPUT_AMOUNT"

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
            inputAmount: BigDecimal? = null
        ) =
            NewSendFragment()
                .withArgs(
                    ARG_RECIPIENT to recipient,
                    ARG_INITIAL_TOKEN to initialToken,
                    ARG_INPUT_AMOUNT to inputAmount,
                )
    }

    private val recipient: SearchResult by args(ARG_RECIPIENT)
    private val initialToken: Token.Active? by args(ARG_INITIAL_TOKEN)
    private val inputAmount: BigDecimal? by args(ARG_INPUT_AMOUNT)

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: NewSendContract.Presenter by inject {
        parametersOf(recipient)
    }

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
            focusAndShowKeyboard()
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
                    val fee = result.getParcelable<SendSolanaFee>(KEY_RESULT_FEE)
                    val feePayerTokens = result.getParcelableArrayList<Token.Active>(KEY_RESULT_FEE_PAYER_TOKENS)
                    if (fee == null || feePayerTokens == null) return@setFragmentResultListener
                    showAccountCreationFeeInfo(fee, feePayerTokens)
                }
            }
        }
    }

    private fun handleSupportFragmentResult(result: Bundle) {
        when {
            // will be more!
            result.containsKey(KEY_RESULT_TOKEN_TO_SEND) -> {
                val token = result.getParcelable<Token.Active>(KEY_RESULT_TOKEN_TO_SEND)!!
                presenter.updateToken(token)
            }
            result.containsKey(KEY_RESULT_NEW_FEE_PAYER) -> {
                val newFeePayer = result.getParcelable<Token.Active>(KEY_RESULT_NEW_FEE_PAYER)!!
                presenter.updateFeePayerToken(newFeePayer)
            }
        }
    }

    override fun showTransactionDetails(sendFeeTotal: SendFeeTotal) {
        NewSendDetailsBottomSheet.show(
            fm = childFragmentManager,
            totalFee = sendFeeTotal,
            requestKey = KEY_REQUEST_SEND,
            feeResultKey = KEY_RESULT_FEE,
            feePayerTokensResultKey = KEY_RESULT_FEE_PAYER_TOKENS
        )
    }

    override fun showFreeTransactionsInfo() {
        FreeTransactionsDetailsBottomSheet.show(childFragmentManager)
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

    override fun setFeeLabel(text: String?) {
        binding.widgetSendDetails.setFeeLabel(text)
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

    override fun showTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?) {
        addFragment(
            target = NewSelectTokenFragment.create(
                tokens = tokens,
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
        listener?.showTransactionProgress(internalTransactionId, data)
        popBackStackTo(target = NewSearchFragment::class, inclusive = true)
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
            is SearchResult.UsernameFound -> recipient.getFormattedUsername()
            else -> recipient.formattedAddress
        }
        title = toolbarTitle
        setNavigationOnClickListener { popBackStack() }
    }
}
