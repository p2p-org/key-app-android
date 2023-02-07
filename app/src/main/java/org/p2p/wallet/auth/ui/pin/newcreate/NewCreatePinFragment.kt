package org.p2p.wallet.auth.ui.pin.newcreate

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameOpenedFrom
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewCreatePinBinding
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkQuery
import org.p2p.wallet.deeplinks.DeeplinkUtils
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction.PlayAnimation
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding
import javax.crypto.Cipher

private const val VIBRATE_DURATION = 10L

class NewCreatePinFragment :
    BaseMvpFragment<NewCreatePinContract.View, NewCreatePinContract.Presenter>(R.layout.fragment_new_create_pin),
    NewCreatePinContract.View {

    companion object {
        fun create() = NewCreatePinFragment()
    }

    override val presenter: NewCreatePinContract.Presenter by inject()

    private val binding: FragmentNewCreatePinBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val deeplinksManager: AppDeeplinksManager by inject()

    private val biometricWrapper: BiometricPromptWrapper by lazy {
        BiometricPromptWrapper(
            this,
            titleRes = R.string.auth_pin_code_fingerprint_title,
            descriptionRes = R.string.auth_pin_code_fingerprint_description,
            onError = { presenter.createPin(biometricCipher = null) },
            onSuccess = { presenter.createPin(biometricCipher = it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setPinMode(PinMode.CREATE)

        with(binding) {
            toolbar.initToolbar()
            pinView.onPinCompleted = { presenter.setPinCode(it) }
            pinView.onKeyboardClicked = { vibrate(VIBRATE_DURATION) }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            presenter.onBackPressed()
        }

        val deeplinkUri = deeplinksManager.pendingDeeplinkUri ?: return
        if (DeeplinkUtils.hasFastOnboardingDeeplink(deeplinkUri)) {
            deeplinkUri.getQueryParameter(DeeplinkQuery.pinCode)?.let { pinCode ->
                if (pinCode.isNotEmpty()) {
                    // create
                    presenter.setPinCode(pinCode)
                    // confirm
                    presenter.setPinCode(pinCode)
                    deeplinksManager.pendingDeeplinkUri = null
                }
            }
        }
    }

    private fun UiKitToolbar.initToolbar() {
        setOnMenuItemClickListener {
            if (it.itemId == R.id.helpItem) {
                IntercomService.showMessenger()
                true
            } else {
                false
            }
        }
    }

    override fun navigateBack() {
        popBackStack()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.pinView.showLoading(isLoading)
    }

    override fun showCreation() {
        presenter.setPinMode(PinMode.CREATE)

        with(binding) {
            pinView.isEnabled = true
            textViewTitle.setText(R.string.auth_create_wallet_set_up_your_pin)
            pinView.clearPin()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.PIN_CREATE)
    }

    override fun showConfirmation() {
        presenter.setPinMode(PinMode.CONFIRM)

        with(binding) {
            pinView.isEnabled = true
            textViewTitle.text = getString(R.string.auth_create_wallet_confirm_your_pin)
            pinView.clearPin()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.PIN_CONFIRM)
    }

    override fun showConfirmationError() {
        showUiKitSnackBar(messageResId = R.string.auth_create_wallet_pin_code_error)
        binding.pinView.startErrorAnimation()
    }

    override fun lockPinKeyboard() {
        binding.pinView.isEnabled = false
    }

    override fun vibrate(duration: Long) {
        requireContext().vibrate(duration)
    }

    override fun navigateToMain(withAnimation: Boolean) {
        binding.pinView.onSuccessPin()

        val actions = if (withAnimation) {
            arrayListOf<MainFragmentOnCreateAction>(PlayAnimation(R.raw.raw_animation_applause))
        } else {
            arrayListOf()
        }
        popAndReplaceFragment(
            MainFragment.create(actions),
            inclusive = true
        )
    }

    override fun navigateToRegisterUsername() {
        binding.pinView.onSuccessPin()

        popAndReplaceFragment(
            ReserveUsernameFragment.create(ReserveUsernameOpenedFrom.ONBOARDING),
            inclusive = true
        )
    }

    override fun showBiometricDialog(biometricCipher: Cipher) {
        biometricWrapper.authenticate(biometricCipher)
    }
}
