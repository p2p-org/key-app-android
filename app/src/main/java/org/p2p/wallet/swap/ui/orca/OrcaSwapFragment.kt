package org.p2p.wallet.swap.ui.orca

import androidx.activity.addCallback
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.token.Token
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSwapOrcaBinding
import org.p2p.wallet.home.ui.select.SelectTokenFragment
import org.p2p.wallet.swap.analytics.SwapAnalytics
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
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getDrawableCompat
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
private const val EXTRA_OPENED_FROM = "EXTRA_SOURCE"

enum class OrcaSwapOpenedFrom {
    MAIN_SCREEN,
    OTHER
}

class OrcaSwapFragment :
    BaseMvpFragment<OrcaSwapContract.View, OrcaSwapContract.Presenter>(R.layout.fragment_swap_orca),
    OrcaSwapContract.View {

    companion object {
        fun create(source: OrcaSwapOpenedFrom = OrcaSwapOpenedFrom.OTHER): OrcaSwapFragment =
            OrcaSwapFragment()
                .withArgs(EXTRA_OPENED_FROM to source)

        fun create(token: Token, source: OrcaSwapOpenedFrom = OrcaSwapOpenedFrom.OTHER): OrcaSwapFragment =
            OrcaSwapFragment()
                .withArgs(
                    EXTRA_TOKEN to token,
                    EXTRA_OPENED_FROM to source
                )
    }

    private val token: Token? by args(EXTRA_TOKEN)
    private val openedFrom: OrcaSwapOpenedFrom by args(EXTRA_OPENED_FROM)

    override val presenter: OrcaSwapContract.Presenter by inject {
        parametersOf(token)
    }
    private val binding: FragmentSwapOrcaBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val swapAnalytics: SwapAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        swapAnalytics.logSwapScreenStarted(analyticsInteractor.getPreviousScreenName())
    }

    private fun setupViews() = with(binding) {
        // in case of MainFragment, back is handled by MainFragment
        when (openedFrom) {
            OrcaSwapOpenedFrom.OTHER -> {
                amountEditText.focusAndShowKeyboard(force = true)
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                    presenter.onBackPressed()
                }
            }
            OrcaSwapOpenedFrom.MAIN_SCREEN -> {
                toolbar.navigationIcon = null
            }
        }

        toolbar.setNavigationOnClickListener { presenter.onBackPressed() }
        toolbar.setOnMenuItemClickListener { menu ->
            if (menu.itemId == R.id.settingsMenuItem) {
                presenter.loadDataForSettings()
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener false
        }

        sourceImageView.setOnClickListener { presenter.loadTokensForSourceSelection() }
        sourceTextView.setOnClickListener { presenter.loadTokensForSourceSelection() }
        sourceDownImageView.setOnClickListener { presenter.loadTokensForSourceSelection() }

        destinationImageView.setOnClickListener { presenter.loadTokensForDestinationSelection() }
        destinationTextView.setOnClickListener { presenter.loadTokensForDestinationSelection() }
        destinationDownImageView.setOnClickListener { presenter.loadTokensForDestinationSelection() }

        availableTextView.setOnClickListener { presenter.fillMaxAmount() }
        maxTextView.setOnClickListener { presenter.fillMaxAmount() }

        setupAmountFractionListener()

        exchangeImageView.setOnClickListener { presenter.reverseTokens() }
        swapDetails.setOnSlippageClickListener { presenter.loadDataForSettings() }
        swapDetails.setOnPayFeeClickListener { presenter.loadDataForSettings() }
        swapDetails.setOnTransactionFeeClickListener { presenter.onFeeLimitsClicked() }
        swapButton.setOnClickListener { presenter.swapOrConfirm() }

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
        iconRes?.let { binding.swapButton.icon = requireContext().getDrawableCompat(iconRes) }

        if (value.isEmpty()) {
            binding.swapButton.setText(textRes)
        } else {
            val text = getString(textRes, *value)
            binding.swapButton.text = text
        }
    }

    override fun showPrice(data: SwapPrice?) {
        binding.swapDetails.showPrice(data)
    }

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

    override fun closeScreen() {
        popBackStack()
    }

    override fun showNewSourceAmount(amount: String) {
        AmountFractionTextWatcher.uninstallFrom(binding.amountEditText)
        binding.amountEditText.setText(amount)
        binding.amountEditText.setSelection(amount.length)
        setupAmountFractionListener()
    }

    override fun setTotalAmountTextColor(@ColorRes totalAmountTextColor: Int) {
        val colorFromTheme = getColor(totalAmountTextColor)
        binding.availableTextView.setTextColor(colorFromTheme)
        binding.availableTextView.compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(colorFromTheme, PorterDuff.Mode.SRC_IN)
        }
    }

    override fun showSwapDetailsError(errorText: String?) {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Swap.ERROR)
        binding.swapDetails.showError(errorText)
    }

    override fun showAroundValue(aroundValue: BigDecimal) {
        binding.aroundTextView.text = getString(
            R.string.main_send_around_in_usd,
            aroundValue.formatFiat()
        )
    }

    override fun showButtonEnabled(isEnabled: Boolean) {
        binding.swapButton.isEnabled = isEnabled
    }

    override fun setMaxButtonVisible(isVisible: Boolean) {
        binding.maxTextView.isVisible = isVisible
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
        binding.swapButton.isLoadingState = isLoading
    }

    override fun showProgressDialog(transactionId: String, data: ShowProgress?) {
        if (data != null) {
            analyticsInteractor.logScreenOpenEvent(ScreenNames.Swap.PROCESSING)
            ProgressBottomSheet.show(parentFragmentManager, transactionId, data, KEY_REQUEST_SWAP)
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

    override fun showDebugSwapRoute(routeAsString: String) {
        binding.textViewDebugSwapRoute.isVisible = true
        binding.textViewDebugSwapRoute.text = routeAsString
    }

    override fun hideDebugSwapRoute() {
        binding.textViewDebugSwapRoute.isVisible = false
    }

    private fun setupAmountFractionListener() {
        AmountFractionTextWatcher.installOn(binding.amountEditText) {
            presenter.setSourceAmount(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AmountFractionTextWatcher.uninstallFrom(binding.amountEditText)
    }
}
