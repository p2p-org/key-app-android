package org.p2p.wallet.smsinput

import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitFourDigitsLargeInput
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSmsInputBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

abstract class BaseSmsInputFragment :
    BaseMvpFragment<SmsInputContract.View, SmsInputContract.Presenter>(R.layout.fragment_sms_input),
    SmsInputContract.View {

    companion object {
        const val ARG_NEXT_DESTINATION_CLASS = "ARG_NEXT_DESTINATION_CLASS"
        const val ARG_NEXT_DESTINATION_ARGS = "ARG_NEXT_DESTINATION_ARGS"
    }

    protected val binding: FragmentSmsInputBinding by viewBinding()
    override val presenter: SmsInputContract.Presenter by inject()

    private val nextDestinationClass: Class<Fragment> by args(ARG_NEXT_DESTINATION_CLASS)
    private val nextDestinationArgs: Bundle? by args(ARG_NEXT_DESTINATION_ARGS)

    protected open fun onBackPressed() = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            uiKitToolbar.setNavigationOnClickListener { onBackPressed() }
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

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                onBackPressed()
            }
        }
    }

    override fun navigateNext() {
        popAndReplaceFragment(createNextDestination(), inclusive = true)
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

    override fun renderSmsTimerState(timerState: SmsInputContract.Presenter.SmsInputTimerState) {
        when (timerState) {
            SmsInputContract.Presenter.SmsInputTimerState.ResendSmsReady -> {
                binding.resendText.text = getString(R.string.onboarding_sms_input_resend_sms)
                binding.resendText.isClickable = true
                binding.resendText.setTextColor(binding.getColor(R.color.text_sky))
            }
            is SmsInputContract.Presenter.SmsInputTimerState.ResendSmsNotReady -> {
                binding.resendText.text = getString(
                    R.string.onboarding_sms_input_before_resend_sms, timerState.secondsBeforeResend
                )
                binding.resendText.isClickable = false
                binding.resendText.setTextColor(binding.getColor(R.color.text_mountain))
            }
            is SmsInputContract.Presenter.SmsInputTimerState.SmsValidationBlocked -> {
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

    private fun createNextDestination(): Fragment {
        return nextDestinationClass.newInstance().apply {
            arguments = nextDestinationArgs
        }
    }
}
