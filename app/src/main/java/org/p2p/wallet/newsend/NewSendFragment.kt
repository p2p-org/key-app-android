package org.p2p.wallet.newsend

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet.Companion.ARG_RESULT_KEY
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.newsend.noaccount.SendNoAccountFragment
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.send.ui.dialogs.FreeTransactionsDetailsBottomSheet
import org.p2p.wallet.send.ui.dialogs.SendTransactionsDetailsBottomSheet
import org.p2p.wallet.send.ui.search.NewSearchFragment
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val ARG_RECIPIENT = "ARG_RECIPIENT"

private const val KEY_RESULT_APPROXIMATE_FEE_USD = "KEY_RESULT_APPROXIMATE_FEE_USD"
private const val KEY_RESULT_FEE_PAYER = "KEY_RESULT_FEE_PAYER"
private const val KEY_RESULT_TOKEN_TO_SEND = "KEY_RESULT_TOKEN_TO_SEND"
private const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

private const val TITLE_CUT_COUNT = 7

class NewSendFragment :
    BaseMvpFragment<NewSendContract.View, NewSendContract.Presenter>(R.layout.fragment_send_new),
    NewSendContract.View {

    companion object {
        fun create(recipient: SearchResult) =
            NewSendFragment()
                .withArgs(ARG_RECIPIENT to recipient)
    }

    private val recipient: SearchResult by args(ARG_RECIPIENT)

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: NewSendContract.Presenter by inject {
        parametersOf(recipient)
    }
    override val navBarColor: Int = R.color.smoke
    override val statusBarColor: Int = R.color.smoke

    private var listener: RootListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupToolbar()
        binding.widgetSendDetails.apply {
            tokenClickListener = presenter::onTokenClicked
            amountListener = presenter::updateInputAmount
            maxButtonClickListener = presenter::setMaxAmountValue
            switchListener = presenter::switchCurrencyMode
            feeButtonClickListener = presenter::onFeeInfoClicked
            focusAndShowKeyboard()
        }
        binding.sliderSend.onSlideCompleteListener = {
            presenter.send()
        }
        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result -> handleSupportFragmentResult(result) }

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(ARG_RESULT_KEY) -> {
                    val fee = result.getParcelable<SendSolanaFee>(ARG_RESULT_KEY)
                    fee?.let { presenter.onAccountCreationFeeClicked(fee) }
                }
            }
        }
    }

    private fun handleSupportFragmentResult(result: Bundle) {
        when {
            result.containsKey(KEY_RESULT_TOKEN_TO_SEND) -> {
                val token = result.getParcelable<Token.Active>(KEY_RESULT_TOKEN_TO_SEND)!!
                presenter.updateToken(token)
            }
            result.containsKey(KEY_RESULT_APPROXIMATE_FEE_USD) -> {
                val approximateFeeUsd = result.getString(KEY_RESULT_APPROXIMATE_FEE_USD).orEmpty()
                presenter.onChangeFeePayerClicked(approximateFeeUsd)
            }
            result.containsKey(KEY_RESULT_FEE_PAYER) -> {
                val token = result.getParcelable<Token.Active>(KEY_RESULT_FEE_PAYER)!!
                presenter.updateFeePayerToken(token)
            }
        }
    }

    override fun showTransactionDetails(sendFeeTotal: SendFeeTotal) {
        SendTransactionsDetailsBottomSheet.show(childFragmentManager, sendFeeTotal, KEY_REQUEST_SEND, ARG_RESULT_KEY)
    }

    override fun showAccountCreationFeeInfo(tokenSymbol: String, amountInUsd: String, hasAlternativeToken: Boolean) {
        val target = SendNoAccountFragment.create(
            tokenSymbol = tokenSymbol,
            approximateFeeUsd = amountInUsd,
            hasAlternativeFeePayerToken = hasAlternativeToken,
            requestKey = KEY_REQUEST_SEND,
            resultKey = KEY_RESULT_APPROXIMATE_FEE_USD
        )
        replaceFragment(target)
    }

    override fun showFreeTransactionsInfo() {
        FreeTransactionsDetailsBottomSheet.show(childFragmentManager)
    }

    override fun updateInputValue(textValue: String, forced: Boolean, maxSymbolsAllowed: Int) {
        binding.widgetSendDetails.setInput(textValue, forced, maxSymbolsAllowed)
    }

    override fun showToken(token: Token.Active) {
        binding.widgetSendDetails.setToken(token)
    }

    override fun setMaxButtonVisible(isVisible: Boolean) {
        binding.widgetSendDetails.setMaxButtonVisible(isVisible)
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

    override fun showFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.showFeeLoading(isLoading)
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

    override fun showFeePayerTokenSelection(
        tokens: List<Token.Active>,
        currentFeePayerToken: Token.Active,
        approximateFeeUsd: String
    ) {
        addFragment(
            target = NewSelectTokenFragment.create(
                tokens = tokens,
                selectedToken = currentFeePayerToken,
                requestKey = KEY_REQUEST_SEND,
                resultKey = KEY_RESULT_FEE_PAYER,
                title = getString(R.string.send_pick_fee_token_format, approximateFeeUsd)
            ),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
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

    override fun showProgressDialog(internalTransactionId: String, data: ShowProgress) {
        listener?.showTransactionProgress(internalTransactionId, data)
        popBackStackTo(target = NewSearchFragment::class, inclusive = true)
    }

    private fun UiKitToolbar.setupToolbar() {
        title = (recipient as? SearchResult.UsernameFound)?.username
            ?: recipient.addressState.address.cutMiddle(TITLE_CUT_COUNT)
        setNavigationOnClickListener { popBackStack() }
    }
}
