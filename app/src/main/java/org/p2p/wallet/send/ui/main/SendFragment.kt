package org.p2p.wallet.send.ui.main

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import org.koin.android.ext.android.inject
import org.p2p.core.common.TextContainer
import org.p2p.core.glide.GlideManager
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.bottomsheet.ErrorBottomSheet
import org.p2p.wallet.databinding.FragmentSendBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.ui.details.TransactionDetailsFragment
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.home.ui.select.SelectTokenFragment
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendConfirmData
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.send.ui.dialogs.SendConfirmBottomSheet
import org.p2p.wallet.send.ui.network.NetworkSelectionFragment
import org.p2p.wallet.send.ui.search.SearchFragment
import org.p2p.wallet.send.ui.search.SearchFragment.Companion.EXTRA_SEARCH_RESULT
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.ui.EXTRA_RESULT_KEY_DISMISS
import org.p2p.wallet.transaction.ui.ProgressBottomSheet
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.backStackEntryCount
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getClipboardText
import org.p2p.wallet.utils.getDrawableCompat
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone
import java.math.BigDecimal

private const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
private const val EXTRA_TOKEN = "EXTRA_TOKEN"
private const val EXTRA_FEE_PAYER = "EXTRA_FEE_PAYER"

private const val REQUEST_QR_KEY = "REQUEST_QR_KEY"
private const val RESULT_QR_KEY = "RESULT_QR_KEY"

const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

@Deprecated("Will be removed, old design flow")
class SendFragment :
    BaseMvpFragment<SendContract.View, SendContract.Presenter>(R.layout.fragment_send),
    SendContract.View {

    companion object {

        fun create(address: String? = null): SendFragment = SendFragment().withArgs(
            EXTRA_ADDRESS to address
        )

        fun create(initialToken: Token.Active): SendFragment = SendFragment().withArgs(
            EXTRA_TOKEN to initialToken
        )
    }

    override val presenter: SendContract.Presenter by inject()
    private val glideManager: GlideManager by inject()

    private val binding: FragmentSendBinding by viewBinding()

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val sendAnalytics: SendAnalytics by inject()

    private val address: String? by args(EXTRA_ADDRESS)
    private val token: Token.Active? by args(EXTRA_TOKEN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token?.let { presenter.setInitialToken(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.MAIN)
        sendAnalytics.logSendStartedScreen(analyticsInteractor.getPreviousScreenName())
        setupViews()

        setOnResultListener()

        // supportFragmentManager for full screen fragments
        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result -> handleSupportFragmentResult(result) }

        // childFragmentManager for BottomSheets
        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result ->
            if (result.containsKey(EXTRA_RESULT_KEY_DISMISS)) {
                clearScreenData()
                popBackStack()
            }
        }

        presenter.loadInitialData()
        checkClipboard()
    }

    private fun handleSupportFragmentResult(result: Bundle) {
        when {
            result.containsKey(EXTRA_TOKEN) -> {
                val token = result.getParcelable<Token.Active>(EXTRA_TOKEN)
                if (token != null) presenter.setSourceToken(token)
            }
            result.containsKey(EXTRA_FEE_PAYER) -> {
                val token = result.getParcelable<Token.Active>(EXTRA_FEE_PAYER)
                if (token != null) presenter.setFeePayerToken(token)
            }
            result.containsKey(EXTRA_SEARCH_RESULT) -> {
                val searchResult = result.getParcelable<SearchResult>(EXTRA_SEARCH_RESULT)
                if (searchResult != null) presenter.setTargetResult(searchResult)
            }
        }
    }

    override fun onDestroyView() {
        AmountFractionTextWatcher.uninstallFrom(binding.amountEditText)
        super.onDestroyView()
    }

    private fun setupViews() {
        with(binding) {
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener {
                if (backStackEntryCount() > 1) {
                    popBackStack()
                } else {
                    requireActivity().apply {
                        hideKeyboard()
                        onBackPressed()
                    }
                }
            }

            targetTextView.setOnClickListener {
                sendAnalytics.logRecipientScreenOpened()
                addFragment(SearchFragment.create())
            }
            targetImageView.setOnClickListener {
                addFragment(SearchFragment.create())
            }
            messageTextView.setOnClickListener {
                addFragment(SearchFragment.create())
            }

            clearImageView.setOnClickListener { presenter.setTargetResult(result = null) }

            installAmountWatcher()
            val originalTextSize = amountEditText.textSize
            // Use invisible auto size textView to handle editText text size
            amountEditText.doOnTextChanged { text, _, _, _ -> handleAmountTextChanged(text, originalTextSize) }

            sourceImageView.setOnClickListener { presenter.loadTokensForSelection() }
            sourceTextView.setOnClickListener { presenter.loadTokensForSelection() }
            downImageView.setOnClickListener { presenter.loadTokensForSelection() }

            accountFeeView.setOnClickListener { presenter.loadFeePayerTokens() }

            availableTextView.setOnClickListener { presenter.setMaxSourceAmountValue() }

            maxTextView.setOnClickListener { presenter.setMaxSourceAmountValue() }

            aroundTextView.setOnClickListener { presenter.switchCurrency() }

            scanTextView.setOnClickListener { presenter.onScanClicked() }

            pasteTextView.setOnClickListener {
                val nameOrAddress = requireContext().getClipboardText(trimmed = true)
                nameOrAddress?.let { presenter.validateTargetAddress(it) }

                sendAnalytics.logSendPasting()
            }

            sendDetailsView.setOnPaidClickListener { presenter.onFeeClicked() }

            sendButton.setOnClickListener { presenter.sendOrConfirm() }

            if (isVisible) amountEditText.focusAndShowKeyboard()

            address?.let { presenter.validateTargetAddress(it) }
        }
    }

    private fun handleAmountTextChanged(text: CharSequence?, originalTextSize: Float) {
        with(binding) {
            autoSizeHelperTextView.setText(text, TextView.BufferType.EDITABLE)
            amountEditText.post {
                val textSize = if (text.isNullOrBlank()) originalTextSize else autoSizeHelperTextView.textSize
                amountEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
        }
    }

    private fun installAmountWatcher() {
        AmountFractionTextWatcher.installOn(binding.amountEditText) {
            presenter.setNewSourceAmount(it)
        }
    }

    override fun showBiometricConfirmationPrompt(data: SendConfirmData) {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.CONFIRMATION)
        SendConfirmBottomSheet.show(this, data, onConfirmed = { presenter.send() })
    }

    override fun showScanner() {
        replaceFragment(ScanQrFragment.create(REQUEST_QR_KEY, RESULT_QR_KEY))
    }

    override fun showFeeLimitsDialog(maxTransactionsAvailable: Int, remaining: Int) {
        showInfoDialog(
            message = getString(R.string.main_free_transactions_info, maxTransactionsAvailable, remaining),
            primaryButtonRes = R.string.common_understood
        )
    }

    // TODO: remove add fragment
    override fun navigateToNetworkSelection(currentNetworkType: NetworkType) {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.NETWORK)
        addFragment(NetworkSelectionFragment.create(currentNetworkType))
    }

    override fun navigateToTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?) {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.FEE_CURRENCY)

        val fragment = NewSelectTokenFragment.create(
            tokens = tokens,
            selectedToken = selectedToken,
            requestKey = KEY_REQUEST_SEND,
            resultKey = EXTRA_TOKEN
        )
        addFragment(
            target = fragment,
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
            targetTextView.setText(R.string.main_key_app_username_sol_address)
            targetTextView.setTextColor(colorFromTheme(R.attr.colorSecondary))

            messageTextView.isVisible = false
            clearImageView.isVisible = false
            scanTextView.isVisible = true
            pasteTextView.isVisible = true
        }
    }

    override fun showWrongAddressTarget(address: String) {
        with(binding) {
            targetImageView.setBackgroundResource(R.drawable.bg_error_rounded_12)
            targetImageView.setImageResource(R.drawable.ic_error)
            targetTextView.text = address
            targetTextView.setTextColor(getColor(R.color.textIconPrimary))

            messageTextView.withTextOrGone(getString(R.string.send_no_address))
            messageTextView.setTextColor(getColor(R.color.systemErrorMain))
            clearImageView.isVisible = true
            scanTextView.isVisible = false
            pasteTextView.isVisible = false
        }
    }

    override fun showUsernameTarget(address: String, username: String, isKeyAppUsername: Boolean) {
        with(binding) {
            if (isKeyAppUsername) {
                targetImageView.setBackgroundResource(org.p2p.uikit.R.drawable.bg_rounded_solid_lime_12)
                targetImageView.setImageResource(R.drawable.ic_key_app_logo)
            } else {
                targetImageView.setBackgroundResource(R.drawable.bg_rounded_solid_rain_12)
                targetImageView.setImageResource(R.drawable.ic_wallet_night)
            }

            targetTextView.text = username
            targetTextView.setTextColor(getColor(R.color.textIconPrimary))

            messageTextView.withTextOrGone(address.cutEnd())
            messageTextView.setTextColor(getColor(R.color.backgroundDisabled))

            clearImageView.isVisible = true
            scanTextView.isVisible = false
            pasteTextView.isVisible = false
        }
    }

    override fun showEmptyBalanceTarget(address: String) {
        with(binding) {
            targetImageView.setBackgroundResource(R.drawable.bg_error_rounded_12)
            targetImageView.setImageResource(R.drawable.ic_warning)
            targetTextView.text = address
            targetTextView.setTextColorRes(R.color.text_night)

            messageTextView.withTextOrGone(getString(R.string.send_empty_balance))
            messageTextView.setTextColorRes(R.color.text_sun)

            clearImageView.isVisible = true
            scanTextView.isVisible = false
            pasteTextView.isVisible = false
        }
    }

    override fun showAddressOnlyTarget(address: String) {
        with(binding) {
            targetImageView.setBackgroundResource(R.drawable.bg_rounded_solid_rain_12)
            targetImageView.setImageResource(R.drawable.ic_wallet_night)
            targetTextView.text = address.cutEnd()
            targetTextView.setTextColor(getColor(R.color.textIconPrimary))

            messageTextView.isVisible = false

            clearImageView.isVisible = true
            scanTextView.isVisible = false
            pasteTextView.isVisible = false
        }
    }

    override fun showAccountFeeView(fee: SendSolanaFee) {
        with(binding) {
            val tokenSymbol = fee.sourceTokenSymbol
            accountInfoTextView.text = getString(R.string.send_account_creation_info, tokenSymbol, tokenSymbol)
            accountInfoTextView.isVisible = true
            accountCardView.isVisible = true

            accountFeeView.showFee(fee)
        }
    }

    override fun hideAccountFeeView() {
        binding.accountCardView.isVisible = false
        binding.accountInfoTextView.isVisible = false
    }

    override fun showInsufficientFundsView(tokenSymbol: String, feeUsd: BigDecimal?) {
        with(binding) {
            accountInfoTextView.text = getString(R.string.send_account_creation_info, tokenSymbol, tokenSymbol)
            accountInfoTextView.isVisible = true
            accountCardView.isVisible = true
            accountFeeView.showInsufficientView(feeUsd)
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
        if (isSuccess) {
            showSuccessSnackBar(getString(R.string.send_transaction_success, tokenAmount))
        } else {
            showErrorSnackBar(getString(R.string.send_transaction_error, tokenAmount))
        }
    }

    override fun showTransactionDetails(transaction: HistoryTransaction) {
        val state = TransactionDetailsLaunchState.History(transaction)
        popAndReplaceFragment(TransactionDetailsFragment.create(state))
    }

    override fun showSourceToken(token: Token.Active) {
        with(binding) {
            glideManager.load(sourceImageView, token.iconUrl)
            sourceTextView.text = token.tokenSymbol
            availableTextView.text = token.getFormattedTotal(includeSymbol = true)
        }
    }

    override fun showTotal(data: SendFeeTotal?) {
        binding.sendDetailsView.showTotal(data)
    }

    override fun showDetailsError(@StringRes errorTextRes: Int?) {
        binding.sendDetailsView.showError(errorTextRes)
    }

    override fun showInputValue(value: BigDecimal, forced: Boolean) {
        with(binding.amountEditText) {
            val textValue = value.toPlainString()
            if (forced) {
                AmountFractionTextWatcher.uninstallFrom(this)
                setText(textValue)
                setSelection(textValue.length)
                installAmountWatcher()
            } else {
                setText(textValue)
                setSelection(textValue.length)
            }
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.sendButton.isLoadingState = isLoading
    }

    override fun showProgressDialog(transactionId: String, data: ShowProgress?) {
        if (data != null) {
            ProgressBottomSheet.show(childFragmentManager, transactionId, data, KEY_REQUEST_SEND)
        } else {
            ProgressBottomSheet.hide(childFragmentManager)
        }
    }

    override fun setMaxButtonVisibility(isVisible: Boolean) {
        binding.maxTextView.isVisible = isVisible
    }

    override fun showIndeterminateLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showAccountFeeViewLoading(isLoading: Boolean) {
        binding.accountFeeView.setLoading(isLoading)
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun setTotalAmountTextColor(@ColorRes textColor: Int) = with(binding.availableTextView) {
        setTextColor(getColor(textColor))
        setTextDrawableColor(textColor)
    }

    override fun showAvailableValue(available: BigDecimal, symbol: String) {
        val formatted = if (symbol == USD_READABLE_SYMBOL) {
            getString(R.string.main_send_around_in_usd, available.formatFiat())
        } else {
            "${available.formatToken()} $symbol"
        }

        binding.availableTextView.text = formatted
    }

    override fun showButtonText(textRes: Int, iconRes: Int?, vararg value: String) {
        iconRes?.let { binding.sendButton.icon = requireContext().getDrawableCompat(iconRes) }

        if (value.isEmpty()) {
            binding.sendButton.setText(textRes)
        } else {
            val text = getString(textRes, *value)
            binding.sendButton.text = text
        }
    }

    @SuppressLint("SetTextI18n")
    override fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String) {
        binding.aroundTextView.text = "${tokenValue.formatToken()} $symbol"
    }

    override fun showUsdAroundValue(usdValue: BigDecimal) {
        binding.aroundTextView.text = getString(R.string.main_send_around_in_usd, usdValue.formatFiat())
    }

    override fun showButtonEnabled(isEnabled: Boolean) {
        binding.sendButton.isEnabled = isEnabled
    }

    override fun showWrongWalletError() {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.ERROR)
        ErrorBottomSheet.show(
            fragment = this,
            iconRes = R.drawable.ic_wallet_error,
            title = TextContainer(R.string.main_send_wrong_wallet),
            message = TextContainer(R.string.main_send_wrong_wallet_message)
        )
    }

    override fun showWarning(messageRes: Int?) {
        if (messageRes != null) {
            binding.warningView.isVisible = true
            binding.warningTextView.setText(messageRes)
        } else {
            binding.warningView.isVisible = false
        }
    }

    private fun setOnResultListener() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            REQUEST_QR_KEY, viewLifecycleOwner
        ) { _, bundle ->
            bundle.getString(RESULT_QR_KEY)?.let { address ->
                presenter.validateTargetAddress(address)
            }
        }
    }

    private fun clearScreenData() {
        with(binding) {
            amountEditText.setText(emptyString())
            clearImageView.callOnClick()
        }
    }

    private fun checkClipboard() {
        val clipboardData = requireContext().getClipboardText()
        binding.pasteTextView.isEnabled = !clipboardData.isNullOrBlank()
    }

    private fun TextView.setTextDrawableColor(@ColorRes color: Int) {
        compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(
                getColor(color),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}
