package org.p2p.wallet.auth.ui.smsinput

import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.components.UiKitFourDigitsLargeInput
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.generalerror.GeneralErrorScreenError
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorFragment
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputContract.Presenter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewSmsInputBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class NewSmsInputFragment :
    BaseMvpFragment<NewSmsInputContract.View, Presenter>(R.layout.fragment_new_sms_input),
    NewSmsInputContract.View {

    companion object {
        fun create() = NewSmsInputFragment()
    }

    override val presenter: Presenter by inject { parametersOf(this) }

    private val binding: FragmentNewSmsInputBinding by viewBinding()

    override fun initView(userPhoneNumber: String) {
        with(binding) {
            checkNumberTitleText.text = getString(R.string.onboarding_sms_input_phone_number_title, userPhoneNumber)
            uiKitToolbar.setNavigationOnClickListener { popBackStack() }
            uiKitToolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.helpItem) {
                    IntercomService.showMessenger()
                    return@setOnMenuItemClickListener true
                }
                false
            }

            smsInputComponent.setInputListener(object : UiKitFourDigitsLargeInput.Listener {
                override fun onInputChanged(inputValue: String) {
                    presenter.onSmsInputChanged(inputValue)
                }
            })

            resendText.setOnClickListener {
                presenter.resendSms()
                it.isClickable = false
            }

            continueButton.setOnClickListener {
                binding.smsInputComponent.clearErrorState()
                presenter.checkSmsValue(smsInputComponent.inputText)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    override fun renderSmsFormatValid() {
        binding.continueButton.setText(R.string.common_continue)
        binding.continueButton.isEnabled = true
    }

    override fun renderSmsFormatInvalid() {
        binding.continueButton.setText(R.string.onboarding_sms_invalid_sms_button_text)
        binding.continueButton.isEnabled = false
    }

    override fun renderIncorrectSms() {
        binding.continueButton.isEnabled = true
        binding.smsInputComponent.setErrorState(getString(R.string.onboarding_sms_input_wrong_sms))
    }

    override fun renderSmsTimerState(timerState: Presenter.SmsInputTimerState) {
        when (timerState) {
            Presenter.SmsInputTimerState.ResendSmsReady -> {
                binding.resendText.text = getString(R.string.onboarding_sms_input_resend_sms)
                binding.resendText.isClickable = true
                binding.resendText.setTextColor(binding.getColor(R.color.text_sky))
            }
            is Presenter.SmsInputTimerState.ResendSmsNotReady -> {
                binding.resendText.text = getString(
                    R.string.onboarding_sms_input_before_resend_sms, timerState.secondsBeforeResend
                )
                binding.resendText.isClickable = false
                binding.resendText.setTextColor(binding.getColor(R.color.text_mountain))
            }
            is Presenter.SmsInputTimerState.SmsValidationBlocked -> {
                binding.smsInputComponent.setErrorState(
                    getString(R.string.onboarding_sms_input_request_overflow, timerState.secondsBeforeUnlock)
                )
                binding.continueButton.isEnabled = false
            }
        }
    }

    override fun renderButtonLoading(isLoading: Boolean) {
        binding.continueButton.isLoadingState = isLoading
    }

    override fun navigateToPinCreate() {
        popAndReplaceFragment(NewCreatePinFragment.create(), inclusive = true)
    }

    override fun navigateToSmsInputBlocked(error: GeneralErrorTimerScreenError) {
        replaceFragment(
            OnboardingGeneralErrorTimerFragment.create(error)
        )
    }

    override fun navigateToCriticalErrorScreen(errorCode: Int) {
        popAndReplaceFragment(
            OnboardingGeneralErrorFragment.create(GeneralErrorScreenError.CriticalError(errorCode)),
            inclusive = true
        )
    }
}
