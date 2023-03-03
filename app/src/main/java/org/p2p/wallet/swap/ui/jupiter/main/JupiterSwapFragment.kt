package org.p2p.wallet.swap.ui.jupiter.main

import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.util.UUID
import org.p2p.core.common.bind
import org.p2p.core.token.Token
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.components.ScreenTab
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrInvisible
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapBinding
import org.p2p.wallet.deeplinks.MainTabsSwitcher
import org.p2p.wallet.swap.jupiter.statemanager.price_impact.SwapPriceImpact
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsFragment
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensFragment
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensListMode
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

class JupiterSwapFragment :
    BaseMvpFragment<JupiterSwapContract.View, JupiterSwapContract.Presenter>(R.layout.fragment_jupiter_swap),
    JupiterSwapContract.View,
    JupiterTransactionBottomSheetDismissListener {

    companion object {
        fun create(token: Token.Active? = null, source: SwapOpenedFrom = SwapOpenedFrom.OTHER): JupiterSwapFragment =
            JupiterSwapFragment()
                .withArgs(
                    EXTRA_TOKEN to token,
                    EXTRA_OPENED_FROM to source
                )
    }

    private val stateManagerHolderKey: String = UUID.randomUUID().toString()
    private val initialToken: Token.Active? by args(EXTRA_TOKEN)
    private val binding: FragmentJupiterSwapBinding by viewBinding()
    private val openedFrom: SwapOpenedFrom by args(EXTRA_OPENED_FROM)
    override val presenter: JupiterSwapContract.Presenter by inject {
        parametersOf(initialToken, stateManagerHolderKey)
    }

    private var mainTabsSwitcher: MainTabsSwitcher? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainTabsSwitcher = parentFragment as? MainTabsSwitcher
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
            val onBackPressed = { presenter.onBackPressed() }
            toolbar.setNavigationOnClickListener { onBackPressed() }
            when (openedFrom) {
                SwapOpenedFrom.MAIN_SCREEN -> {
                    toolbar.navigationIcon = null
                }
                SwapOpenedFrom.OTHER -> {
                    requireActivity().onBackPressedDispatcher
                        .addCallback(viewLifecycleOwner) { onBackPressed() }
                }
            }

            sliderSend.onSlideCompleteListener = { presenter.onSwapSliderClicked() }

            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.settingsMenuItem) {
                    openSwapSettingsScreen()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setupWidgetsActionCallbacks() = with(binding) {
        swapWidgetFrom.onAmountChanged = { presenter.onTokenAmountChange(it) }
        swapWidgetFrom.onAllAmountClick = { presenter.onAllAmountClick() }
        swapWidgetFrom.onChangeTokenClick = { presenter.onChangeTokenAClick() }
        swapWidgetTo.onChangeTokenClick = { presenter.onChangeTokenBClick() }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            insets.systemAndIme().consume {
                binding.toolbar.appleTopInsets(this)
                binding.scrollView.appleBottomInsets(this)
                binding.frameLayoutSliderSend.appleBottomInsets(this)
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

    override fun openChangeTokenAScreen() {
        val fragment = SwapTokensFragment.create(SwapTokensListMode.TOKEN_A, stateManagerHolderKey)
        replaceFragment(fragment)
    }

    override fun openChangeTokenBScreen() {
        val fragment = SwapTokensFragment.create(SwapTokensListMode.TOKEN_B, stateManagerHolderKey)
        replaceFragment(fragment)
    }

    fun openSwapSettingsScreen() {
        val fragment = JupiterSwapSettingsFragment.create(stateManagerKey = stateManagerHolderKey)
        replaceFragment(fragment)
    }

    override fun showPriceImpact(priceImpact: SwapPriceImpact) {
        when (priceImpact) {
            SwapPriceImpact.NORMAL -> Unit
            SwapPriceImpact.YELLOW -> setYellowAlert()
            SwapPriceImpact.RED -> setRoseAlert()
        }
        binding.linearLayoutAlert.isVisible = priceImpact != SwapPriceImpact.NORMAL
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

    override fun showCompleteSlider() {
        binding.sliderSend.showCompleteAnimation()
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
                    message = getString(R.string.swap_main_slippage_changed, result.newSlippageValue.toString()),
                    actionButtonResId = R.string.swap_main_slippage_changed_details_button,
                    actionBlock = { openSwapSettingsScreen() }
                )
            }
            JupiterTransactionDismissResult.TrySwapAgain -> Unit
        }
    }

    private fun navigateBackOnTransactionSuccess() {
        when (openedFrom) {
            SwapOpenedFrom.MAIN_SCREEN -> {
                presenter.reloadFeature()
                mainTabsSwitcher?.navigate(ScreenTab.HOME_SCREEN)
            }
            SwapOpenedFrom.OTHER -> {
                popBackStack()
            }
        }
    }

    private fun setYellowAlert() {
        val context = binding.root.context
        DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(8f.toPx())),
            tint = R.color.bg_light_sun,
            strokeWidth = 1f.toPx(),
            strokeColor = R.color.bg_sun,
        ).applyBackground(binding.linearLayoutAlert)
        binding.imageViewAlert.imageTintList = context.getColorStateList(R.color.icons_sun)
        binding.textViewAlert.setTextColor(context.getColorStateList(R.color.text_night))
    }

    private fun setRoseAlert() {
        val context = binding.root.context
        DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(8f.toPx())),
            tint = R.color.light_rose,
            strokeWidth = 1f.toPx(),
            strokeColor = R.color.bg_rose,
        ).applyBackground(binding.linearLayoutAlert)
        binding.imageViewAlert.imageTintList = context.getColorStateList(R.color.icons_rose)
        binding.textViewAlert.setTextColor(context.getColorStateList(R.color.text_rose))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.finishFeature(stateManagerHolderKey)
    }

    override fun closeScreen() {
        popBackStack()
    }
}
