package org.p2p.wallet.newsend

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

private const val ARG_RECIPIENT_ADDRESS = "ARG_RECIPIENT_ADDRESS"
private const val ARG_RECIPIENT_USERNAME = "ARG_RECIPIENT_USERNAME"

private const val KEY_RESULT_TOKEN_TO_SEND = "KEY_RESULT_TOKEN_TO_SEND"
private const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

class NewSendFragment :
    BaseMvpFragment<NewSendContract.View, NewSendContract.Presenter>(R.layout.fragment_send_new),
    NewSendContract.View {

    companion object {
        fun create(recipientAddress: Base58String, recipientUsername: String?) =
            NewSendFragment()
                .withArgs(
                    ARG_RECIPIENT_ADDRESS to recipientAddress.base58Value,
                    ARG_RECIPIENT_USERNAME to recipientUsername
                )
    }

    private val recipientAddress: String by args(ARG_RECIPIENT_ADDRESS)
    private val recipientUsername: String? by args(ARG_RECIPIENT_USERNAME)

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: NewSendContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.apply {
            title = recipientUsername ?: recipientAddress
            setNavigationOnClickListener { popBackStack() }
        }
        binding.widgetSendDetails.apply {
            tokenClickListener = presenter::onTokenClicked
            amountListener = presenter::setAmount
            maxButtonClickListener = presenter::setMaxAmountValue
            switchListener = presenter::switchCurrencyMode
            focusAndShowKeyboard()
        }
        // TODO PWN-6090 make button
        binding.sliderSend.setActionText(R.string.send_enter_amount)

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

    override fun setMaxButtonVisibility(isVisible: Boolean) {
        binding.widgetSendDetails.setMaxButtonVisibility(isVisible)
    }

    override fun showAroundValue(value: String) {
        binding.widgetSendDetails.setAroundValue(value)
    }

    override fun showFeeViewLoading(isLoading: Boolean) {
        // TODO PWN-6090 Progress on fee View
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
}
