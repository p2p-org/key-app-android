package org.p2p.wallet.auth.ui.generalerror

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.components.UiKitButton
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ButtonAction
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.Presenter
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.View as ContractView

private const val ARG_ERROR_STATE = "ARG_ERROR_STATE"

class OnboardingGeneralErrorFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_error),
    ContractView {

    companion object {

        fun create(error: GatewayHandledState) =
            OnboardingGeneralErrorFragment()
                .withArgs(ARG_ERROR_STATE to error)
    }

    private val binding: FragmentOnboardingGeneralErrorBinding by viewBinding()

    private val screenError: GatewayHandledState by args(ARG_ERROR_STATE)

    override val presenter: Presenter by inject { parametersOf(screenError) }

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night
    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setState(state: GatewayHandledState.TitleSubtitleError): Unit = with(binding) {
        textViewErrorTitle.text = state.title
        textViewErrorSubtitle.text = state.subtitle
        state.email?.let {
            textViewErrorEmail.text = it
            textViewErrorEmail.isVisible = true
        }

        state.googleButton?.let { buttonState ->
            buttonRestoreByGoogle.setText(buttonState.titleResId)
            buttonState.iconResId?.let { buttonRestoreByGoogle.setIconResource(it) }
            if (buttonState.iconTintResId == null) {
                buttonRestoreByGoogle.icon = null
            } else {
                buttonRestoreByGoogle.setIconResource(buttonState.iconTintResId)
            }
            setButtonAction(buttonRestoreByGoogle, buttonState.buttonAction)
            buttonRestoreByGoogle.isVisible = true
        }
        state.primaryFirstButton?.let { buttonState ->
            buttonPrimaryFirst.setText(buttonState.titleResId)
            setButtonAction(buttonPrimaryFirst, buttonState.buttonAction)
            buttonPrimaryFirst.isVisible = true
        }
        state.secondaryFirstButton?.let { buttonState ->
            buttonSecondaryFirst.setText(buttonState.titleResId)
            setButtonAction(buttonSecondaryFirst, buttonState.buttonAction)
            buttonSecondaryFirst.isVisible = true
        }
    }

    private fun setButtonAction(button: UiKitButton, action: ButtonAction) {
        button.setOnClickListener {
            when (action) {
                ButtonAction.OPEN_INTERCOM -> {
                    IntercomService.showMessenger()
                }
                ButtonAction.NAVIGATE_ENTER_PHONE -> {
                    popAndReplaceFragment(
                        PhoneNumberEnterFragment.create(),
                        inclusive = true
                    )
                }
                ButtonAction.NAVIGATE_START_SCREEN -> {
                    popAndReplaceFragment(
                        OnboardingRootFragment.create(),
                        inclusive = true
                    )
                }
            }
        }
    }

    override fun setState(state: GatewayHandledState.CriticalError) {
        // TODO
    }
}
