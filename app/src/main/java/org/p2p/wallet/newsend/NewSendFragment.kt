package org.p2p.wallet.newsend

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

private const val ARG_RECIPIENT = "ARG_RECIPIENT"

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

    override val presenter: NewSendContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupToolbar()
        binding.widgetSendDetails.apply {
            tokenClickListener = presenter::onTokenClicked
            amountListener = presenter::setAmount
            maxButtonClickListener = presenter::setMaxAmountValue
            switchListener = presenter::switchCurrencyMode
            setFeeLabel(getString(R.string.send_fees))
            focusAndShowKeyboard()
        }
        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result -> handleSupportFragmentResult(result) }
    }

    private fun handleSupportFragmentResult(result: Bundle) {
        when {
            // will be more!
            result.containsKey(KEY_RESULT_TOKEN_TO_SEND) -> {
                val token = result.getParcelable<Token.Active>(KEY_RESULT_TOKEN_TO_SEND)
                token?.let {
                    presenter.setTokenToSend(it)
                }
            }
        }
    }

    override fun showInputValue(value: BigDecimal, forced: Boolean) {
        binding.widgetSendDetails.setInput(value, forced)
    }

    override fun showTokenToSend(token: Token.Active) {
        binding.widgetSendDetails.setToken(token)
    }

    override fun setMaxButtonIsVisible(isVisible: Boolean) {
        binding.widgetSendDetails.setMaxButtonVisibility(isVisible)
    }

    override fun setBottomButtonText(text: String) {
        binding.buttonBottom.text = text
    }

    override fun setBottomButtonIsVisible(isVisible: Boolean) {
        binding.buttonBottom.isVisible = isVisible
        binding.sliderSend.isVisible = !isVisible
    }

    override fun setSliderText(text: String) {
        binding.sliderSend.setActionText(text)
    }

    override fun showAroundValue(value: String) {
        binding.widgetSendDetails.setAroundValue(value)
    }

    override fun showFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.setFeeProgressIsVisible(isLoading)
    }

    override fun setFeeLabel(text: String) {
        binding.widgetSendDetails.setFeeLabel(text)
    }

    override fun showInsufficientFundsView(tokenSymbol: String, feeUsd: BigDecimal?) {
        // TODO PWN-6090 Bottom button
    }

    override fun setSwitchLabel(symbol: String) {
        binding.widgetSendDetails.setSwitchLabel(getString(R.string.send_switch_to_token, symbol))
    }

    override fun setMainAmountLabel(symbol: String) {
        binding.widgetSendDetails.setMainAmountLabel(symbol)
    }

    override fun navigateToTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?) {
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

    private fun UiKitToolbar.setupToolbar() {
        title = (recipient as? SearchResult.UsernameFound)?.username
            ?: recipient.addressState.address.cutMiddle(TITLE_CUT_COUNT)
        setNavigationOnClickListener { popBackStack() }
    }
}
