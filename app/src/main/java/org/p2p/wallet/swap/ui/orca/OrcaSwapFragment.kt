package org.p2p.wallet.swap.ui.orca

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.textwatcher.AmountFractionTextWatcher
import org.p2p.wallet.databinding.FragmentSwapOrcaBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.ui.details.TransactionDetailsFragment
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.SelectTokenFragment
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.SwapConfirmData
import org.p2p.wallet.swap.model.orca.OrcaSettingsResult
import org.p2p.wallet.swap.model.orca.SwapFee
import org.p2p.wallet.swap.model.orca.SwapPrice
import org.p2p.wallet.swap.model.orca.SwapTotal
import org.p2p.wallet.swap.ui.settings.SwapSettingsFragment
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.ui.EXTRA_RESULT_KEY_DISMISS
import org.p2p.wallet.transaction.ui.ProgressBottomSheet
import org.p2p.wallet.utils.AmountUtils
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.focusAndShowKeyboard
import org.p2p.wallet.utils.getColor
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

const val KEY_REQUEST_SWAP = "KEY_REQUEST_SWAP"
private const val EXTRA_SOURCE_TOKEN = "EXTRA_SOURCE_TOKEN"
private const val EXTRA_DESTINATION_TOKEN = "EXTRA_DESTINATION_TOKEN"
private const val EXTRA_SETTINGS = "EXTRA_SETTINGS"

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class OrcaSwapFragment :
    BaseMvpFragment<OrcaSwapContract.View, OrcaSwapContract.Presenter>(R.layout.fragment_swap_orca),
    OrcaSwapContract.View {

    companion object {
        fun create() = OrcaSwapFragment()

        fun create(token: Token) = OrcaSwapFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token? by args(EXTRA_TOKEN)

    override val presenter: OrcaSwapContract.Presenter by inject {
        parametersOf(token)
    }
    private val binding: FragmentSwapOrcaBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressedCallback = requireActivity().onBackPressedDispatcher.addCallback {
            presenter.onBackPressed()
        }

        setupViews()

        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SWAP,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(EXTRA_SOURCE_TOKEN) -> {
                    val token = result.getParcelable<Token.Active>(EXTRA_SOURCE_TOKEN)
                    if (token != null) presenter.setNewSourceToken(token)
                }
                result.containsKey(EXTRA_DESTINATION_TOKEN) -> {
                    val token = result.getParcelable<Token>(EXTRA_DESTINATION_TOKEN)
                    if (token != null) presenter.setNewDestinationToken(token)
                }
                result.containsKey(EXTRA_SETTINGS) -> {
                    val settingsResult = result.getParcelable<OrcaSettingsResult>(EXTRA_SETTINGS)
                    if (settingsResult != null) presenter.setNewSettings(settingsResult)
                }
                result.containsKey(EXTRA_RESULT_KEY_DISMISS) -> {
                    popBackStack()
                }
            }
        }

        presenter.loadInitialData()
    }

    private fun setupViews() = with(binding) {
        toolbar.setNavigationOnClickListener { presenter.onBackPressed() }
        toolbar.setOnMenuItemClickListener { menu ->
            if (menu.itemId == R.id.settingsMenuItem) {
                presenter.loadDataForSettings()
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener false
        }
        sourceImageView.setOnClickListener { presenter.loadTokensForSourceSelection() }
        destinationImageView.setOnClickListener { presenter.loadTokensForDestinationSelection() }
        destinationTextView.setOnClickListener { presenter.loadTokensForDestinationSelection() }
        availableTextView.setOnClickListener { presenter.calculateAvailableAmount() }
        maxTextView.setOnClickListener { presenter.calculateAvailableAmount() }

        setupAmountListener()

        exchangeImageView.setOnClickListener { presenter.reverseTokens() }
        swapDetails.setOnSlippageClickListener {
            presenter.loadDataForSettings()
        }
        swapDetails.setOnPayFeeClickListener {
            presenter.loadDataForSettings()
        }
        swapDetails.setOnTransactionFeeClickListener {
            presenter.onFeeLimitsClicked()
        }
        swapButton.setOnClickListener { presenter.swapOrConfirm() }
        amountEditText.focusAndShowKeyboard()

        val originalTextSize = amountEditText.textSize

        // Use invisible auto size textView to handle editText text size
        amountEditText.doOnTextChanged { text, _, _, _ ->
            autoSizeHelperTextView.setText(text, TextView.BufferType.EDITABLE)
            amountEditText.post {
                val textSize =
                    if (text.isNullOrBlank()) originalTextSize
                    else autoSizeHelperTextView.textSize

                amountEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        AmountFractionTextWatcher.uninstallFrom(binding.amountEditText)
    }

    override fun showSourceToken(token: Token.Active) {
        with(binding) {
            Glide.with(sourceImageView).load(token.iconUrl).into(sourceImageView)
            sourceTextView.text = token.tokenSymbol
            availableTextView.isVisible = true
            availableTextView.text = token.getFormattedTotal()
            swapDetails.showTotal(null)
        }
    }

    override fun showDestinationToken(token: Token?) {
        with(binding) {
            if (token != null) {
                Glide.with(destinationImageView).load(token.iconUrl).into(destinationImageView)
                destinationTextView.text = token.tokenSymbol
                destinationAvailableTextView.isVisible = token is Token.Active
                if (token is Token.Active) destinationAvailableTextView.text = token.getFormattedTotal()
            } else {
                destinationImageView.setImageResource(R.drawable.ic_question)
                destinationTextView.setText(R.string.main_select)
                destinationAvailableTextView.isVisible = false
                destinationAvailableTextView.text = emptyString()
            }
        }
    }

    override fun showButtonText(textRes: Int, iconRes: Int?, vararg value: String) {
        binding.swapButton.setStartIcon(iconRes)

        if (value.isEmpty()) {
            binding.swapButton.setActionText(textRes)
        } else {
            val text = getString(textRes, *value)
            binding.swapButton.setActionText(text)
        }
    }

    override fun setNewAmount(sourceAmount: String) {
        binding.amountEditText.setText(sourceAmount)
    }

    @SuppressLint("SetTextI18n")
    override fun showPrice(data: SwapPrice?) {
        binding.swapDetails.showPrice(data)
    }

    @SuppressLint("SetTextI18n")
    override fun showTotal(data: SwapTotal?) {
        with(binding) {
            swapDetails.showTotal(data)

            if (data != null) {
                receiveAtLeastLabelTextView.text = getString(R.string.main_swap_min_receive)
                receiveTextView.text = data.receiveAtLeast
                receiveUsdTextView.text = data.receiveAtLeastUsd
                destinationAmountTextView.text = data.destinationAmount
            } else {
                receiveAtLeastLabelTextView.text = emptyString()
                receiveTextView.text = emptyString()
                receiveUsdTextView.text = emptyString()
                destinationAmountTextView.text = emptyString()
            }
        }
    }

    override fun showFees(data: SwapFee?) {
        binding.swapDetails.showFee(data)
    }

    override fun showFeePayerToken(feePayerTokenSymbol: String) {
        binding.swapDetails.showFeePayerToken(feePayerTokenSymbol)
    }

    override fun showSlippage(slippage: Slippage) {
        binding.swapDetails.showSlippage(slippage)
    }

    override fun showBiometricConfirmationPrompt(data: SwapConfirmData) {
        SwapConfirmBottomSheet.show(this, data) { presenter.swap() }
    }

    override fun close() {
        popBackStack()
    }

    override fun showNewAmount(amount: String) {
        AmountFractionTextWatcher.uninstallFrom(binding.amountEditText)
        binding.amountEditText.setText(amount)
        binding.amountEditText.setSelection(amount.length)
        setupAmountListener()
    }

    override fun setAvailableTextColor(@ColorRes availableColor: Int) {
        val colorFromTheme = getColor(availableColor)
        binding.availableTextView.setTextColor(colorFromTheme)
        binding.availableTextView.compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(colorFromTheme, PorterDuff.Mode.SRC_IN)
        }
    }

    override fun showError(@StringRes errorText: Int?) {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Swap.ERROR)
        binding.swapDetails.showError(errorText)
    }

    override fun showAroundValue(aroundValue: BigDecimal) {
        binding.aroundTextView.text = getString(
            R.string.main_send_around_in_usd,
            AmountUtils.format(aroundValue)
        )
    }

    override fun showButtonEnabled(isEnabled: Boolean) {
        binding.swapButton.isEnabled = isEnabled
    }

    override fun showTransactionStatusMessage(fromSymbol: String, toSymbol: String, isSuccess: Boolean) {
        if (isSuccess) {
            showSuccessSnackBar(getString(R.string.swap_transaction_completed, fromSymbol, toSymbol))
        } else {
            showErrorSnackBar(getString(R.string.swap_transaction_failed, fromSymbol, toSymbol))
        }
    }

    override fun showTransactionDetails(transaction: HistoryTransaction) {
        val state = TransactionDetailsLaunchState.History(transaction)
        popAndReplaceFragment(TransactionDetailsFragment.create(state))
    }

    override fun showSourceSelection(tokens: List<Token.Active>) {
        addFragment(
            target = SelectTokenFragment.create(tokens, KEY_REQUEST_SWAP, EXTRA_SOURCE_TOKEN),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showDestinationSelection(tokens: List<Token>) {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Swap.CURRENCY_B)
        addFragment(
            target = SelectTokenFragment.create(tokens, KEY_REQUEST_SWAP, EXTRA_DESTINATION_TOKEN),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showSwapSettings(
        currentSlippage: Slippage,
        tokens: List<Token.Active>,
        currentFeePayerToken: Token.Active
    ) {
        val target = SwapSettingsFragment.create(currentSlippage, tokens, currentFeePayerToken, EXTRA_SETTINGS)
        addFragment(target)
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showLoading(isLoading: Boolean) {
        binding.swapButton.setLoading(isLoading)
    }

    override fun showProgressDialog(data: ShowProgress?) {
        if (data != null) {
            analyticsInteractor.logScreenOpenEvent(ScreenNames.Swap.PROCESSING)
            ProgressBottomSheet.show(parentFragmentManager, data, KEY_REQUEST_SWAP)
        } else {
            ProgressBottomSheet.hide(parentFragmentManager)
        }
    }

    override fun showFeeLimitsDialog(maxTransactionsAvailable: Int, remaining: Int) {
        showInfoDialog(
            message = getString(R.string.main_free_transactions_info, maxTransactionsAvailable, remaining),
            primaryButtonRes = R.string.common_understood
        )
    }

    private fun setupAmountListener() {
        AmountFractionTextWatcher.installOn(binding.amountEditText) {
            presenter.setSourceAmount(it)
        }
    }
}
