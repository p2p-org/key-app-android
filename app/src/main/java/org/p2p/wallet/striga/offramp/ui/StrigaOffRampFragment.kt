package org.p2p.wallet.striga.offramp.ui

import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import java.math.BigDecimal
import org.p2p.uikit.components.UiKitButtonIconState
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrInvisible
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaOffRampBinding
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.striga.StrigaFragmentFactory
import org.p2p.wallet.striga.offramp.StrigaOffRampContract
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getDrawable
import org.p2p.wallet.utils.viewbinding.viewBinding

private typealias FragmentContract = BaseMvpFragment<
    StrigaOffRampContract.View,
    StrigaOffRampContract.Presenter
    >

class StrigaOffRampFragment :
    FragmentContract(R.layout.fragment_striga_off_ramp),
    StrigaOffRampContract.View {

    companion object {
        fun create(): StrigaOffRampFragment = StrigaOffRampFragment()
    }

    override val presenter: StrigaOffRampContract.Presenter by inject()
    private val strigaFragmentFactory: StrigaFragmentFactory by inject()

    private val binding: FragmentStrigaOffRampBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupView()
    }

    override fun setTokenAWidgetState(state: SwapWidgetModel) {
        binding.swapWidgetFrom.bind(state)
    }

    override fun setTokenBWidgetState(state: SwapWidgetModel) {
        binding.swapWidgetTo.bind(state)
    }

    override fun setRatioState(state: TextViewCellModel?) {
        binding.textViewRate.bindOrInvisible(state)
    }

    override fun setButtonState(buttonState: StrigaOffRampButtonState) {
        with(binding.buttonNext) {
            setText(buttonState.titleResId)
            isEnabled = buttonState.isEnabled
            isClickable = buttonState.isClickable

            // this is the rare case, when we can't just use setLoading(bool)
            // because setLoading overwrites its icon and restores previous one
            // when it set to false, but here we need to set icon and set loading independently
            when {
                buttonState.isLoading -> {
                    setIconState(UiKitButtonIconState.Loading())
                }
                buttonState.isEnabled -> {
                    background.setTint(binding.getColor(buttonState.styleEnabledBgColorRes))
                    setTextColor(binding.getColor(buttonState.styleEnabledTextColorRes))
                    setIconState(
                        UiKitButtonIconState.Icon(binding.getDrawable(buttonState.iconDrawableResId))
                    )
                }
                else -> {
                    background.setTint(binding.getColor(buttonState.styleDisabledBgColorRes))
                    setTextColor(binding.getColor(buttonState.styleDisabledTextColorRes))
                    setIconState(UiKitButtonIconState.None)
                }
            }
        }
    }

    override fun setTokenATextColorRes(textColorRes: Int) {
        binding.swapWidgetFrom.setAmountTextColorRes(textColorRes)
    }

    override fun navigateToSignup(destination: StrigaUserStatusDestination) {
        strigaFragmentFactory.signupFlowFragment(destination)?.let(::replaceFragment)
    }

    override fun navigateToWithdraw(amountInUsdc: BigDecimal) {
        strigaFragmentFactory.withdrawUsdcFragment(amountInUsdc).let(::replaceFragment)
    }

    override fun showUiKitSnackBar(
        message: String?,
        messageResId: Int?,
        onDismissed: () -> Unit,
        actionButtonResId: Int?,
        actionBlock: ((Snackbar) -> Unit)?
    ) {
        require(message != null || messageResId != null) {
            "Snackbar text must be set from `message` or `messageResId` params"
        }
        val snackbarText: String = message ?: messageResId?.let(::getString)!!
        val root = requireView().rootView
        if (actionButtonResId != null && actionBlock != null) {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                actionButtonText = getString(actionButtonResId),
                actionButtonListener = actionBlock,
                enableBottomNavOffset = false
            )
        } else {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                onDismissed = onDismissed,
                enableBottomNavOffset = false
            )
        }
    }

    private fun FragmentStrigaOffRampBinding.setupView() {
        setupWidgetActions()
        toolbar.setNavigationOnClickListener {
            popBackStack()
        }
        buttonNext.setOnClickListener {
            presenter.onSubmit()
        }
        imageViewSwapTokens.background = shapeDrawable(shapeCircle())
        imageViewSwapTokens.backgroundTintList = requireContext().getColorStateList(R.color.button_rain)

        // todo: hack! this should be fixed somewhere in SwapWidget.bind()
        // This fixes behavior when user clicks on input and cursor sets before the initial 0,
        // thus user by entering any amount will overwrite the initial 0 instead of appending digits to it.
        // In JupiterSwap the same behavior is fixed by setting initial amount to 0 directly to the EditText
        // see JupiterSwapPresenter.attach() -> view.setAmountFiat(String)
        swapWidgetFrom.setAmount("0")
        swapWidgetTo.setAmount("0")
    }

    private fun setupWidgetActions() {
        with(binding) {
            swapWidgetFrom.onAmountChanged = presenter::onTokenAAmountChange
            swapWidgetFrom.onAllAmountClick = presenter::onAllAmountClick
            swapWidgetTo.onAmountChanged = presenter::onTokenBAmountChange
        }
    }
}
