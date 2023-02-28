package org.p2p.wallet.swap.ui.jupiter.main

import androidx.activity.addCallback
import androidx.core.view.isInvisible
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
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapBinding
import org.p2p.wallet.home.isInMainTabsScreen
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsFragment
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensFragment
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensChangeToken
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class JupiterSwapFragment :
    BaseMvpFragment<JupiterSwapContract.View, JupiterSwapContract.Presenter>(R.layout.fragment_jupiter_swap),
    JupiterSwapContract.View {

    companion object {
        fun create(token: Token.Active? = null): JupiterSwapFragment =
            JupiterSwapFragment()
                .withArgs(
                    EXTRA_TOKEN to token,
                )
    }

    private val stateManagerHolderKey: String = UUID.randomUUID().toString()
    private val token: Token.Active? by args(EXTRA_TOKEN)
    private val binding: FragmentJupiterSwapBinding by viewBinding()
    override val presenter: JupiterSwapContract.Presenter by inject {
        parametersOf(token, stateManagerHolderKey)
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
            if (isInMainTabsScreen()) {
                toolbar.navigationIcon = null
            } else {
                requireActivity().onBackPressedDispatcher
                    .addCallback(viewLifecycleOwner) { onBackPressed() }
            }

            sliderSend.onSlideCompleteListener = { sliderSend.showCompleteAnimation() }
            sliderSend.onSlideCollapseCompleted = { presenter.onSwapTokenClick() }

            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.settingsMenuItem -> {
                        openSwapSettingsScreen()
                        true
                    }
                    else -> false
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
                sliderSend.isInvisible = true
                buttonError.bind(buttonState.text)
            }
            is SwapButtonState.Hide -> {
                buttonError.isInvisible = true
                sliderSend.isInvisible = true
            }
            is SwapButtonState.ReadyToSwap -> {
                buttonError.isInvisible = true
                sliderSend.isInvisible = false
                sliderSend.setActionText(buttonState.text)
            }
        }
    }

    override fun setRatioState(state: TextViewCellModel) {
        binding.textViewRate.bind(state)
    }

    override fun openChangeTokenAScreen() {
        val fragment = SwapTokensFragment.create(SwapTokensChangeToken.TOKEN_A, stateManagerHolderKey)
        replaceFragment(fragment)
    }

    override fun openChangeTokenBScreen() {
        val fragment = SwapTokensFragment.create(SwapTokensChangeToken.TOKEN_B, stateManagerHolderKey)
        replaceFragment(fragment)
    }

    fun openSwapSettingsScreen() {
        val fragment = JupiterSwapSettingsFragment.create(stateManagerKey = stateManagerHolderKey)
        replaceFragment(fragment)
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
