package org.p2p.wallet.send.ui.main

import android.annotation.SuppressLint
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.text.buildSpannedString
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.bottomsheet.ErrorBottomSheet
import org.p2p.wallet.common.ui.bottomsheet.TextContainer
import org.p2p.wallet.databinding.FragmentSendBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.details.TransactionDetailsFragment
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.SelectTokenFragment
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendConfirmData
import org.p2p.wallet.send.model.SendFee
import org.p2p.wallet.send.model.SendTotal
import org.p2p.wallet.send.ui.dialogs.EXTRA_NETWORK
import org.p2p.wallet.send.ui.dialogs.NetworkSelectionFragment
import org.p2p.wallet.send.ui.search.SearchFragment
import org.p2p.wallet.send.ui.search.SearchFragment.Companion.EXTRA_RESULT
import org.p2p.wallet.send.ui.dialogs.SendConfirmBottomSheet
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.ui.ProgressBottomSheet
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.backStackEntryCount
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.focusAndShowKeyboard
import org.p2p.wallet.utils.getClipBoardText
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone
import java.math.BigDecimal

private const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
private const val EXTRA_TOKEN = "EXTRA_TOKEN"
private const val EXTRA_FEE_PAYER = "EXTRA_FEE_PAYER"
const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

class SendFragment :
    BaseMvpFragment<SendContract.View, SendContract.Presenter>(R.layout.fragment_send),
    SendContract.View {

    companion object {

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

    private val glideManager: GlideManager by inject()

    private val binding: FragmentSendBinding by viewBinding()

    private val address: String? by args(EXTRA_ADDRESS)

    private val token: Token? by args(EXTRA_TOKEN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(EXTRA_TOKEN) -> {
                    val token = result.getParcelable<Token.Active>(EXTRA_TOKEN)
                    if (token != null) presenter.setSourceToken(token)
                }
                result.containsKey(EXTRA_FEE_PAYER) -> {
                    val token = result.getParcelable<Token.Active>(EXTRA_FEE_PAYER)
                    if (token != null) presenter.setFeePayerToken(token)
                }
                result.containsKey(EXTRA_RESULT) -> {
                    val searchResult = result.getParcelable<SearchResult>(EXTRA_RESULT)
                    if (searchResult != null) presenter.setTargetResult(searchResult)
                }
                result.containsKey(EXTRA_NETWORK) -> {
                    val ordinal = result.getInt(EXTRA_NETWORK, 0)
                    presenter.setNetworkDestination(NetworkType.values()[ordinal])
                }
            }
        }

        presenter.loadInitialData()
        checkClipBoard()
    }

    private fun setupViews() {
        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                scrollView.fit { Edge.BottomArc }
            }

            if (backStackEntryCount() > 1) {
                toolbar.setNavigationIcon(R.drawable.ic_back)
                toolbar.setNavigationOnClickListener { popBackStack() }
            }
            sendButton.setOnClickListener { presenter.sendOrConfirm() }

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
                presenter.loadCurrentNetwork()
            }

            sourceImageView.setOnClickListener {
                presenter.loadTokensForSelection()
            }

            address?.let { presenter.validateTarget(it) }

            amountEditText.focusAndShowKeyboard()

            accountFeeView.setOnClickListener {
                presenter.loadFeePayerTokens()
            }

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
                val nameOrAddress = requireContext().getClipBoardText(trimmed = true)
                nameOrAddress?.let { presenter.validateTarget(it) }
            }

            sendDetailsView.setOnPaidClickListener {
                showInfoDialog(
                    messageRes = R.string.main_free_transactions_info,
                    primaryButtonRes = R.string.common_understood
                )
            }
        }
    }

    override fun showBiometricConfirmationPrompt(data: SendConfirmData) {
        SendConfirmBottomSheet.show(this, data) { presenter.send() }
    }

    // TODO: remove add fragment
    override fun navigateToNetworkSelection(currentNetworkType: NetworkType) {
        addFragment(NetworkSelectionFragment.create(currentNetworkType))
    }

    override fun navigateToTokenSelection(tokens: List<Token.Active>) {
        addFragment(
            target = SelectTokenFragment.create(tokens, KEY_REQUEST_SEND, EXTRA_TOKEN),
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
            targetImageView.setBackgroundResource(R.drawable.bg_error_rounded)
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
            targetImageView.setBackgroundResource(R.drawable.bg_error_rounded)
            targetImageView.setImageResource(R.drawable.ic_warning)
            targetTextView.text = address
            targetTextView.setTextColor(colorFromTheme(R.attr.colorMessagePrimary))

            messageTextView.withTextOrGone(getString(R.string.send_caution_empty_balance))
            messageTextView.setTextColor(requireContext().getColor(R.color.systemWarning))
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

    override fun showAccountFeeView(fee: SendFee?) {
        with(binding) {
            if (fee == null) {
                accountCardView.isVisible = false
                accountInfoTextView.isVisible = false
                return
            }

            accountInfoTextView.isVisible = true
            accountCardView.isVisible = true

            val feeUsd = if (fee.feeUsd != null) "~$${fee.feeUsd}" else getString(R.string.common_not_available)
            accountFeeTextView.text = getString(R.string.send_account_creation_fee_format, feeUsd)
            accountFeeValueTextView.text = fee.formattedFee
            glideManager.load(accountImageView, fee.feePayerToken.iconUrl)
        }
    }

    override fun showSearchScreen(usernames: List<SearchResult>) {
        addFragment(SearchFragment.create(usernames))
    }

    override fun showFeePayerTokenSelector(feePayerTokens: List<Token.Active>) {
        addFragment(
            target = SelectTokenFragment.create(feePayerTokens, KEY_REQUEST_SEND, EXTRA_FEE_PAYER),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showTransactionStatusMessage(amount: BigDecimal, symbol: String, isSuccess: Boolean) {
        val tokenAmount = "$amount $symbol"
        val (message, iconRes) = if (isSuccess) {
            getString(R.string.send_transaction_success, tokenAmount) to R.drawable.ic_done
        } else {
            getString(R.string.send_transaction_error, tokenAmount) to R.drawable.ic_close_red
        }
        showSnackbar(message, iconRes)
    }

    override fun showTransactionDetails(transaction: HistoryTransaction) {
        popAndReplaceFragment(TransactionDetailsFragment.create(transaction))
    }

    override fun showNetworkDestination(type: NetworkType) {
        when (type) {
            NetworkType.SOLANA -> {
                binding.networkImageView.setImageResource(R.drawable.ic_sol)
                binding.networkNameTextView.setText(R.string.send_solana_network_title)

                val transactionFee = getString(R.string.send_transaction_fee)
                val zeroUsd = getString(R.string.send_zero_usd)
                val commonText = "$transactionFee: $zeroUsd"

                val startIndex = commonText.indexOf(zeroUsd)
                val color = requireContext().getColor(R.color.systemSuccess)
                val highlightText = buildSpannedString {
                    append(commonText)

                    setSpan(
                        ForegroundColorSpan(color),
                        startIndex,
                        startIndex + zeroUsd.length,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )

                    setSpan(
                        StyleSpan(BOLD),
                        startIndex,
                        startIndex + zeroUsd.length,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }

                binding.networkFeeTextView withTextOrGone highlightText
            }
            NetworkType.BITCOIN -> {
                binding.networkImageView.setImageResource(R.drawable.ic_btc_rounded)
                binding.networkNameTextView.setText(R.string.send_bitcoin_network_title)
                // TODO: add renBTC fee
                binding.networkFeeTextView withTextOrGone null
            }
        }
    }

    override fun showNetworkSelectionView(isVisible: Boolean) {
        binding.networkView.isVisible = isVisible
    }

    override fun showSourceToken(token: Token.Active) {
        with(binding) {
            glideManager.load(sourceImageView, token.iconUrl)
            sourceTextView.text = token.tokenSymbol
            availableTextView.text = token.getFormattedTotal()
        }
    }

    override fun showTotal(data: SendTotal?) {
        binding.sendDetailsView.showTotal(data)
    }

    override fun showInputValue(value: BigDecimal) {
        val textValue = "$value"
        binding.amountEditText.setText(textValue)
        binding.amountEditText.setSelection(textValue.length)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.sendButton.setLoading(isLoading)
    }

    override fun showProgressDialog(data: ShowProgress?) {
        if (data != null) {
            ProgressBottomSheet.show(childFragmentManager, data)
        } else {
            ProgressBottomSheet.hide(childFragmentManager)
        }
    }

    override fun showSearchLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun updateAvailableTextColor(@ColorRes availableColor: Int) {
        binding.availableTextView.setTextColor(requireContext().getColor(availableColor))
    }

    @SuppressLint("SetTextI18n")
    override fun showAvailableValue(available: BigDecimal, symbol: String) {
        binding.availableTextView.text = "${available.scaleLong()} $symbol"
    }

    override fun showButtonText(textRes: Int, iconRes: Int?, vararg value: String) {
        binding.sendButton.setStartIcon(iconRes)

        if (value.isEmpty()) {
            binding.sendButton.setActionText(textRes)
        } else {
            val text = getString(textRes, *value)
            binding.sendButton.setActionText(text)
        }
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

    override fun showSuccessMessage(amount: String) {
        val message = getString(R.string.send_successful_format, amount)
        showSnackbarMessage(message)
    }

    private fun checkClipBoard() {
        val clipBoardData = requireContext().getClipBoardText()
        binding.pasteTextView.isEnabled = !clipBoardData.isNullOrBlank()
    }
}