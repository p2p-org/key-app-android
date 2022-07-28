package org.p2p.wallet.auth.ui.pin.newcreate

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.auth.ui.pin.select.TouchIdFragment
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewCreatePinBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding

class NewCreatePinFragment :
    BaseMvpFragment<NewCreatePinContract.View, NewCreatePinContract.Presenter>(R.layout.fragment_new_create_pin),
    NewCreatePinContract.View {

    companion object {
        fun create() = NewCreatePinFragment()
    }

    override val presenter: NewCreatePinContract.Presenter by inject()

    private val binding: FragmentNewCreatePinBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { presenter.clearUserData() }
            pinView.onPinCompleted = {
                presenter.setPinCode(it)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStackTo(OnboardingFragment::class)
        }
    }

    override fun navigateBack() {
        popBackStack()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.pinView.showLoading(isLoading)
    }

    override fun showCreation() {
        with(binding) {
            pinView.isEnabled = true
            textViewTitle.setText(R.string.auth_create_wallet_set_up_your_pin)
            pinView.clearPin()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.PIN_CREATE)
    }

    override fun showConfirmation() {
        with(binding) {
            pinView.isEnabled = true
            textViewTitle.text = getString(R.string.auth_create_wallet_confirm_your_pin)
            pinView.clearPin()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.PIN_CONFIRM)
    }

    override fun onPinCreated(pinCode: String) {
        val fragment = TouchIdFragment.create(pinCode)
        binding.pinView.onSuccessPin()
        binding.root.showSnackbarShort(R.string.auth_create_wallet_pin_code_success) {
            replaceFragment(fragment)
        }
    }

    override fun showConfirmationError() {
        binding.root.showSnackbarShort(R.string.auth_create_wallet_pin_code_error)
        binding.pinView.startErrorAnimation()
    }

    override fun lockPinKeyboard() {
        binding.pinView.isEnabled = false
    }

    override fun vibrate(duration: Long) {
        requireContext().vibrate(duration)
    }
}
