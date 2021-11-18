package org.p2p.wallet.main.ui.send

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.bottomsheet.ErrorBottomSheet
import org.p2p.wallet.common.ui.bottomsheet.TextContainer
import org.p2p.wallet.databinding.FragmentSendBinding
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.model.SearchResult
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.select.SelectTokenFragment
import org.p2p.wallet.main.ui.send.search.SearchFragment
import org.p2p.wallet.main.ui.send.search.SearchFragment.Companion.EXTRA_RESULT
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import org.p2p.wallet.main.ui.transaction.TransactionStatusBottomSheet
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.focusAndShowKeyboard
import org.p2p.wallet.utils.getClipBoard
import org.p2p.wallet.utils.getClipboardData
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone
import java.math.BigDecimal

class SendFragment :
    BaseMvpFragment<SendContract.View, SendContract.Presenter>(R.layout.fragment_send),
    SendContract.View {

    companion object {
        private const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"
        fun create(address: String? = null) = SendFragment().withArgs(
            EXTRA_ADDRESS to address
        )

        fun create(initialToken: Token) = SendFragment().withArgs(
            EXTRA_TOKEN to initialToken
        )
    }

    override val presenter: SendContract.Presenter by inject {
        parametersOf(token)
    }

    private val binding: FragmentSendBinding by viewBinding()

    private val address: String? by args(EXTRA_ADDRESS)

    private val token: Token? by args(EXTRA_TOKEN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            sendButton.setOnClickListener { presenter.send() }

            targetTextView.setOnClickListener {
                addFragment(SearchFragment.create())
            }
            targetImageView.setOnClickListener {
                addFragment(SearchFragment.create())
            }
            messageTextView.setOnClickListener {
                addFragment(SearchFragment.create())
            }

            clearImageView.setOnClickListener {
                presenter.setTargetResult(null)
            }

            amountEditText.doAfterTextChanged {
                presenter.setNewSourceAmount(it.toString())
            }

            networkView.setOnClickListener {
                NetworkDestinationBottomSheet.show(childFragmentManager) { presenter.setNetworkDestination(it) }
            }

            sourceImageView.setOnClickListener {
                presenter.loadTokensForSelection()
            }

            address?.let { presenter.validateTarget(it) }

            amountEditText.focusAndShowKeyboard()

            availableTextView.setOnClickListener {
                presenter.loadAvailableValue()
            }

            aroundTextView.setOnClickListener {
                presenter.switchCurrency()
            }

            scanTextView.setOnClickListener {
                val target = ScanQrFragment.create { presenter.validateTarget(it) }
                addFragment(target)
            }

            pasteTextView.setOnClickListener {
                val nameOrAddress = requireContext().getClipboardData()
                nameOrAddress?.let { presenter.validateTarget(it) }
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner,
            { _, result ->
                when {
                    result.containsKey(EXTRA_TOKEN) -> {
                        val token = result.getParcelable<Token.Active>(EXTRA_TOKEN)
                        if (token != null) presenter.setSourceToken(token)
                    }
                    result.containsKey(EXTRA_RESULT) -> {
                        val searchResult = result.getParcelable<SearchResult>(EXTRA_RESULT)
                        if (searchResult != null) presenter.setTargetResult(searchResult)
                    }
                }
            }
        )

        presenter.loadInitialData()
        checkClipBoard()
    }

    override fun navigateToTokenSelection(tokens: List<Token.Active>) {
        addFragment(
            target = SelectTokenFragment.create(tokens),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showIdleTarget() {
        with(binding) {
            targetImageView.setBackgroundResource(R.drawable.bg_gray_secondary_rounded_small)
            targetImageView.setImageResource(R.drawable.ic_search)
            targetTextView.setText(R.string.main_p2p_username_sol_address)
            targetTextView.setTextColor(colorFromTheme(R.attr.colorSecondary))

            messageTextView.isVisible = false
            clearImageView.isVisible = false
        }
    }

    override fun showWrongAddressTarget(address: String) {
        with(binding) {
            targetImageView.setBackgroundResource(R.drawable.bg_warning_rounded)
            targetImageView.setImageResource(R.drawable.ic_error)
            targetTextView.text = address
            targetTextView.setTextColor(colorFromTheme(R.attr.colorMessagePrimary))

            messageTextView.withTextOrGone(getString(R.string.send_no_address))
            messageTextView.setTextColor(colorFromTheme(R.attr.colorAccentWarning))
            clearImageView.isVisible = true
        }
    }

    override fun showFullTarget(address: String, username: String) {
        with(binding) {
            targetImageView.setBackgroundResource(R.drawable.bg_blue_rounded_medium)
            targetImageView.setImageResource(R.drawable.ic_wallet_white)
            targetTextView.text = username
            targetTextView.setTextColor(colorFromTheme(R.attr.colorMessagePrimary))

            messageTextView.withTextOrGone(address.cutEnd())
            messageTextView.setTextColor(colorFromTheme(R.attr.colorElementSecondary))
            clearImageView.isVisible = true
        }
    }

    override fun showEmptyBalanceTarget(address: String) {
        with(binding) {
            targetImageView.setBackgroundResource(R.drawable.bg_warning_rounded)
            targetImageView.setImageResource(R.drawable.ic_warning)
            targetTextView.text = address
            targetTextView.setTextColor(colorFromTheme(R.attr.colorMessagePrimary))

            messageTextView.withTextOrGone(getString(R.string.send_caution_empty_balance))
            messageTextView.setTextColor(requireContext().getColor(R.color.colorWarning))
            clearImageView.isVisible = true
        }
    }

    override fun showAddressOnlyTarget(address: String) {
        with(binding) {
            targetImageView.setBackgroundResource(R.drawable.bg_blue_rounded_medium)
            targetImageView.setImageResource(R.drawable.ic_wallet_white)
            targetTextView.text = address.cutEnd()
            targetTextView.setTextColor(colorFromTheme(R.attr.colorMessagePrimary))

            messageTextView.isVisible = false
            clearImageView.isVisible = true
        }
    }

    override fun showSuccess(info: TransactionInfo) {
        TransactionStatusBottomSheet.show(
            fragment = this,
            info = info,
            onDismiss = { popBackStack() }
        )
    }

    override fun showNetworkDestination(type: NetworkType) {
        binding.networkTextView.text = type.stringValue
    }

    override fun showNetworkSelection() {
        binding.networkView.isVisible = true
        TransitionManager.beginDelayedTransition(binding.containerView)
    }

    override fun hideNetworkSelection() {
        binding.networkView.isVisible = false
        TransitionManager.beginDelayedTransition(binding.containerView)
    }

    override fun showSourceToken(token: Token.Active) {
        with(binding) {
            Glide.with(sourceImageView).load(token.logoUrl).into(sourceImageView)
            sourceTextView.text = token.tokenSymbol
            availableTextView.text = token.getFormattedTotal()
            priceTextView.text = token.getCurrentPrice()
        }
    }

    override fun showFee(fee: String?) {
        if (fee.isNullOrEmpty()) {
            binding.feeTextView.setText(R.string.send_free_transaction)
            binding.feeTextView.setTextColor(requireContext().getColor(R.color.colorGreen))
            binding.feeTextView.setOnClickListener { FeeInfoBottomSheet.show(childFragmentManager) }
        } else {
            binding.feeTextView.text = fee
            binding.feeTextView.setTextColor(colorFromTheme(R.attr.colorMessagePrimary))
            binding.feeTextView.setOnClickListener(null)
        }
    }

    override fun showInputValue(value: BigDecimal) {
        val textValue = "$value"
        binding.amountEditText.setText(textValue)
        binding.amountEditText.setSelection(textValue.length)
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            sendButton.setLoading(isLoading)
        }
    }

    override fun showSearchLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun setAvailableTextColor(availableColor: Int) {
        binding.availableTextView.setTextColor(colorFromTheme(availableColor))
    }

    @SuppressLint("SetTextI18n")
    override fun showAvailableValue(available: BigDecimal, symbol: String) {
        binding.availableTextView.text = "$available $symbol"
    }

    override fun showButtonText(textRes: Int) {
        binding.sendButton.setActionText(textRes)
    }

    @SuppressLint("SetTextI18n")
    override fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String) {
        binding.aroundTextView.text = "$tokenValue $symbol"
    }

    override fun showUsdAroundValue(usdValue: BigDecimal) {
        binding.aroundTextView.text = getString(R.string.main_send_around_in_usd, usdValue)
    }

    override fun showButtonEnabled(isEnabled: Boolean) {
        binding.sendButton.isEnabled = isEnabled
    }

    override fun showWrongWalletError() {
        ErrorBottomSheet.show(
            fragment = this,
            iconRes = R.drawable.ic_wallet_error,
            title = TextContainer(R.string.main_send_wrong_wallet),
            message = TextContainer(R.string.main_send_wrong_wallet_message)
        )
    }

    private fun checkClipBoard() {
        val clipBoardData = requireContext().getClipBoard()
        binding.pasteTextView.isEnabled = clipBoardData != null
    }
}