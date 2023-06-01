package org.p2p.wallet.auth.ui.smsinput

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitFourDigitsLargeInput
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorFragment
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.ui.restore_error.RestoreErrorScreenFragment
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputContract.Presenter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewSmsInputBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber

class NewSmsInputFragment :
    BaseMvpFragment<NewSmsInputContract.View, Presenter>(R.layout.fragment_new_sms_input),
    NewSmsInputContract.View {

    companion object {
        fun create(): NewSmsInputFragment = NewSmsInputFragment()
    }

    override val presenter: Presenter by inject()
    private val binding: FragmentNewSmsInputBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            uiKitToolbar.setNavigationOnClickListener { popBackStack() }
            uiKitToolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.helpItem) {
                    view.hideKeyboard()
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
            }

            continueButton.setOnClickListener {
                binding.smsInputComponent.clearErrorState()
                presenter.checkSmsValue(smsInputComponent.inputText)
            }
            binding.smsInputComponent.focusAndShowKeyboard()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    override fun initView(userPhoneNumber: PhoneNumber) {
        binding.checkNumberTitleText.text =
            getString(R.string.onboarding_sms_input_phone_number_title, userPhoneNumber.formattedValue)
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
            else -> {
                Timber.i("Unknown sms input state: $timerState")
            }
        }
    }

    override fun renderButtonLoading(isLoading: Boolean) {
        binding.continueButton.setLoading(isLoading)
    }

    override fun navigateToPinCreate() {
        popAndReplaceFragment(NewCreatePinFragment.create(), inclusive = true)
    }

    override fun navigateToSmsInputBlocked(error: GeneralErrorTimerScreenError, timerLeftTime: Long) {
        replaceFragment(
            OnboardingGeneralErrorTimerFragment.create(error, timerLeftTime)
        )
    }

    override fun navigateToGatewayErrorScreen(handledState: GatewayHandledState) {
        popAndReplaceFragment(OnboardingGeneralErrorFragment.create(handledState))
    }

    override fun navigateToRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError) {
        popAndReplaceFragment(RestoreErrorScreenFragment.create(handledState))
    }
}
