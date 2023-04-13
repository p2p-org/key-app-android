package org.p2p.wallet.jupiter.ui.main

import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.util.UUID
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.core.common.bind
import org.p2p.core.token.Token
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.components.ScreenTab
import org.p2p.uikit.utils.context
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind
import org.p2p.uikit.utils.text.bindOrInvisible
import org.p2p.uikit.utils.toPx
import org.p2p.uikit.utils.toast
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapBinding
import org.p2p.wallet.deeplinks.MainTabsSwitcher
import org.p2p.wallet.jupiter.JupiterPresenterInitialData
import org.p2p.wallet.jupiter.analytics.JupiterSwapMainScreenAnalytics
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.jupiter.ui.settings.JupiterSwapSettingsFragment
import org.p2p.wallet.jupiter.ui.tokens.SwapTokensFragment
import org.p2p.wallet.jupiter.ui.tokens.SwapTokensListMode
import org.p2p.wallet.root.ActivityVisibility
import org.p2p.wallet.root.AppActivityVisibility
import org.p2p.wallet.swap.ui.orca.SwapOpenedFrom
import org.p2p.wallet.transaction.ui.JupiterTransactionBottomSheetDismissListener
import org.p2p.wallet.transaction.ui.JupiterTransactionDismissResult
import org.p2p.wallet.transaction.ui.JupiterTransactionProgressBottomSheet
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetData
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"
private const val EXTRA_OPENED_FROM = "EXTRA_OPENED_FROM"
private const val EXTRA_INITIAL_TOKEN_A_SYMBOL = "EXTRA_FROM_TOKEN_NAME"
private const val EXTRA_INITIAL_TOKEN_B_SYMBOL = "EXTRA_TO_TOKEN_NAME"
private const val EXTRA_INITIAL_AMOUNT = "EXTRA_AMOUNT"

class JupiterSwapFragment :
    BaseMvpFragment<JupiterSwapContract.View, JupiterSwapContract.Presenter>(R.layout.fragment_jupiter_swap),
    JupiterSwapContract.View,
    JupiterTransactionBottomSheetDismissListener {

    companion object {
        fun create(token: Token.Active? = null, source: SwapOpenedFrom): JupiterSwapFragment =
            JupiterSwapFragment()
                .withArgs(
                    EXTRA_TOKEN to token,
                    EXTRA_OPENED_FROM to source
                )

        fun create(tokenASymbol: String, tokenBSymbol: String, amount: String, source: SwapOpenedFrom): JupiterSwapFragment =
            JupiterSwapFragment()
                .withArgs(
                    EXTRA_INITIAL_TOKEN_A_SYMBOL to tokenASymbol,
                    EXTRA_INITIAL_TOKEN_B_SYMBOL to tokenBSymbol,
                    EXTRA_INITIAL_AMOUNT to amount,
                    EXTRA_OPENED_FROM to source
                )
    }

    private val stateManagerHolderKey: String = UUID.randomUUID().toString()
    private val initialToken: Token.Active? by args(EXTRA_TOKEN)
    private val initialTokenASymbol: String? by args(EXTRA_INITIAL_TOKEN_A_SYMBOL)
    private val initialTokenBSymbol: String? by args(EXTRA_INITIAL_TOKEN_B_SYMBOL)
    private val initialAmount: String? by args(EXTRA_INITIAL_AMOUNT)
    private val binding: FragmentJupiterSwapBinding by viewBinding()
    private val openedFrom: SwapOpenedFrom by args(EXTRA_OPENED_FROM)
    private val analytics: JupiterSwapMainScreenAnalytics by inject()
    override val presenter: JupiterSwapContract.Presenter by inject {
        parametersOf(
            JupiterPresenterInitialData(
                stateManagerHolderKey,
                openedFrom,
                initialToken,
                initialAmount,
                initialTokenASymbol,
                initialTokenBSymbol,
            )
        )
    }

    private var mainTabsSwitcher: MainTabsSwitcher? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainTabsSwitcher = parentFragment as? MainTabsSwitcher
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppActivityVisibility).visibilityState
            .onEach {
                when (it) {
                    ActivityVisibility.Initializing -> Unit
                    ActivityVisibility.Invisible -> presenter.pauseStateManager()
                    ActivityVisibility.Visible -> presenter.resumeStateManager()
                }
            }
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .launchIn(lifecycleScope)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            setupWidgetsActionCallbacks()
            imageViewSwapTokens.background = shapeDrawable(shapeCircle())
            imageViewSwapTokens.backgroundTintList = view.context.getColorStateList(R.color.button_rain)
            imageViewSwapTokens.rippleForeground(shapeCircle())
            imageViewSwapTokens.setOnClickListener {
                presenter.switchTokens()
            }
            sliderSend.onSlideCompleteListener = { presenter.onSwapSliderClicked() }

            textViewDebug.isVisible = BuildConfig.DEBUG

            setupToolbar()
            buttonTryAgain.setOnClickListener { presenter.onTryAgainClick() }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            presenter.pauseStateManager()
        } else {
            presenter.resumeStateManager()
        }
    }

    private fun FragmentJupiterSwapBinding.setupToolbar() {
        val onBackPressed = { presenter.onBackPressed() }
        toolbar.setNavigationOnClickListener { onBackPressed() }

        when (openedFrom) {
            SwapOpenedFrom.BOTTOM_NAVIGATION -> {
                toolbar.navigationIcon = null
            }
            else -> {
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { onBackPressed() }
            }
        }

        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.settingsMenuItem) {
                openSwapSettingsScreen()
                true
            } else {
                false
            }
        }
    }

    private fun setupWidgetsActionCallbacks() = with(binding) {
        swapWidgetFrom.onAmountChanged = presenter::onTokenAmountChange
        swapWidgetFrom.onAllAmountClick = presenter::onAllAmountClick
        swapWidgetFrom.onChangeTokenClick = presenter::onChangeTokenAClick
        swapWidgetTo.onChangeTokenClick = presenter::onChangeTokenBClick
        swapWidgetTo.onInputClicked = { toast(R.string.swap_tokens_you_pay_only) }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            insets.systemAndIme().consume {
                binding.toolbar.appleTopInsets(this)
                binding.scrollView.appleBottomInsets(this)
                binding.frameLayoutSliderSend.appleBottomInsets(this)
                binding.containerError.appleBottomInsets(this)
            }
        }
    }

    override fun setFirstTokenWidgetState(state: SwapWidgetModel) {
        binding.swapWidgetFrom.bind(state)
    }

    override fun setSecondTokenWidgetState(state: SwapWidgetModel) {
        binding.swapWidgetTo.bind(state)
    }

    override fun setButtonState(buttonState: SwapButtonState) = with(binding) {
        when (buttonState) {
            is SwapButtonState.Disabled -> {
                buttonError.isInvisible = false
                sliderSend.isVisible = false
                buttonError.bind(buttonState.text)
            }
            is SwapButtonState.Hide -> {
                buttonError.isInvisible = true
                sliderSend.isVisible = false
            }
            is SwapButtonState.ReadyToSwap -> {
                buttonError.isInvisible = true
                sliderSend.isVisible = true
                sliderSend.setActionText(buttonState.text)
            }
        }
    }

    override fun setRatioState(state: TextViewCellModel?) {
        binding.textViewRate.bindOrInvisible(state)
    }

    override fun setAmountFiat(amount: String) {
        binding.swapWidgetFrom.setAmount(amount)
    }

    override fun showSolErrorToast() {
        showUiKitSnackBar(message = getString(R.string.swap_main_button_sol_error_toast))
    }

    override fun openChangeTokenAScreen() {
        val fragment = SwapTokensFragment.create(SwapTokensListMode.TOKEN_A, stateManagerHolderKey)
        replaceFragment(fragment)
    }

    override fun openChangeTokenBScreen() {
        val fragment = SwapTokensFragment.create(SwapTokensListMode.TOKEN_B, stateManagerHolderKey)
        replaceFragment(fragment)
    }

    private fun openSwapSettingsScreen() {
        analytics.logSwapSettingsClicked()
        val fragment = JupiterSwapSettingsFragment.create(stateManagerKey = stateManagerHolderKey)
        replaceFragment(fragment)
    }

    override fun showPriceImpact(priceImpact: SwapPriceImpactView) {
        when (priceImpact) {
            SwapPriceImpactView.NORMAL -> Unit
            SwapPriceImpactView.YELLOW -> setYellowAlert()
            SwapPriceImpactView.RED -> setRoseAlert()
        }
        binding.linearLayoutAlert.isVisible = priceImpact != SwapPriceImpactView.NORMAL
    }

    override fun scrollToPriceImpact() {
        binding.scrollView.smoothScrollTo(0, binding.scrollView.height)
    }

    override fun showProgressDialog(internalTransactionId: String, transactionDetails: SwapTransactionBottomSheetData) {
        JupiterTransactionProgressBottomSheet.show(
            fm = childFragmentManager,
            transactionId = internalTransactionId,
            data = transactionDetails
        )
    }

    override fun showDefaultSlider() {
        binding.sliderSend.restoreSlider()
    }

    override fun onBottomSheetDismissed(result: JupiterTransactionDismissResult) {
        when (result) {
            JupiterTransactionDismissResult.TransactionInProgress,
            JupiterTransactionDismissResult.TransactionSuccess -> {
                navigateBackOnTransactionSuccess()
            }
            JupiterTransactionDismissResult.ManualSlippageChangeNeeded -> {
                openSwapSettingsScreen()
            }
            is JupiterTransactionDismissResult.SlippageChangeNeeded -> {
                presenter.changeSlippage(result.newSlippageValue)
                showUiKitSnackBar(
                    message = getString(
                        R.string.swap_main_slippage_changed,
                        result.newSlippageValue.percentValue
                    ),
                    actionButtonResId = R.string.swap_main_slippage_changed_details_button,
                    actionBlock = { openSwapSettingsScreen() }
                )
            }
            JupiterTransactionDismissResult.TrySwapAgain -> presenter.resumeStateManager()
        }
    }

    private fun navigateBackOnTransactionSuccess() {
        when (openedFrom) {
            SwapOpenedFrom.BOTTOM_NAVIGATION -> {
                presenter.reloadFeature()
                mainTabsSwitcher?.navigate(ScreenTab.HOME_SCREEN)
            }
            else -> {
                popBackStack()
            }
        }
    }

    private fun setYellowAlert() = with(binding) {
        DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(8f.toPx())),
            tint = R.color.bg_light_sun,
            strokeWidth = 1f.toPx(),
            strokeColor = R.color.bg_sun,
        ).applyBackground(linearLayoutAlert)
        imageViewAlert.imageTintList = context.getColorStateList(R.color.icons_sun)
        textViewAlert.setTextColor(context.getColorStateList(R.color.text_night))
    }

    private fun setRoseAlert() = with(binding) {
        DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(8f.toPx())),
            tint = R.color.light_rose,
            strokeWidth = 1f.toPx(),
            strokeColor = R.color.bg_rose,
        ).applyBackground(binding.linearLayoutAlert)
        imageViewAlert.imageTintList = context.getColorStateList(R.color.icons_rose)
        textViewAlert.setTextColor(context.getColorStateList(R.color.text_rose))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.finishFeature(stateManagerHolderKey)
    }

    override fun closeScreen() {
        popBackStack()
    }

    override fun showFullScreenError() = with(binding) {
        scrollView.isVisible = false
        frameLayoutSliderSend.isVisible = false
        textViewRate.isVisible = false
        containerError.isVisible = true
    }

    override fun hideFullScreenError() {
        with(binding) {
            scrollView.isVisible = true
            frameLayoutSliderSend.isVisible = true
            textViewRate.isVisible = true
            containerError.isVisible = false
        }
    }

    override fun showDebugInfo(cellModel: TextViewCellModel) {
        binding.textViewDebug.bind(cellModel)
    }

    override fun showKeyboard() {
        if (openedFrom != SwapOpenedFrom.BOTTOM_NAVIGATION) binding.swapWidgetFrom.focusAndShowKeyboard()
    }
}
