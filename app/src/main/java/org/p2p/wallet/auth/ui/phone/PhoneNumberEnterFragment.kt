package org.p2p.wallet.auth.ui.phone

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.generalerror.GeneralErrorScreenErrorType
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorFragment
import org.p2p.wallet.auth.ui.phone.countrypicker.CountryCodePickerDialog
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputFragment
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenErrorType
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentPhoneNumberEnterBinding
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class PhoneNumberEnterFragment :
    BaseMvpFragment<PhoneNumberEnterContract.View, PhoneNumberEnterContract.Presenter>(
        R.layout.fragment_phone_number_enter
    ),
    PhoneNumberEnterContract.View {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
        const val RESULT_KEY = "RESULT_KEY"
        fun create(): PhoneNumberEnterFragment = PhoneNumberEnterFragment()
    }

    override val presenter: PhoneNumberEnterContract.Presenter by inject()
    private val binding: FragmentPhoneNumberEnterBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        buttonConfirmPhone.setOnClickListener {
            presenter.submitUserPhoneNumber(editTextPhoneNumber.text?.toString().orEmpty())
        }

        setOnResultListener()
        presenter.load()
    }

    override fun showDefaultCountryCode(country: CountryCode?) {
        binding.editTextPhoneNumber.setupViewState(
            countryCode = country,
            onCountryCodeChanged = ::onCountryCodeChanged,
            onPhoneChanged = ::onPhoneChanged,
            onCountryClickListener = ::onCountryClickListener
        )
    }

    override fun update(countryCode: CountryCode?) {
        binding.editTextPhoneNumber.updateViewState(countryCode)
    }

    override fun onNewCountryDetected(countryCode: CountryCode) {
        binding.editTextPhoneNumber.onFoundNewCountry(countryCode)
    }

    override fun showCountryCodePicker(selectedCountryCode: CountryCode?) {
        CountryCodePickerDialog.show(selectedCountryCode, REQUEST_KEY, RESULT_KEY, childFragmentManager)
    }

    override fun navigateToSmsInput() {
        replaceFragment(NewSmsInputFragment.create())
    }

    override fun navigateToAccountBlocked() {
        replaceFragment(
            OnboardingGeneralErrorTimerFragment.create(GeneralErrorTimerScreenErrorType.BLOCK_PHONE_NUMBER_ENTER)
        )
    }

    override fun navigateToCriticalErrorScreen() {
        popAndReplaceFragment(
            OnboardingGeneralErrorFragment.create(GeneralErrorScreenErrorType.CRITICAL_ERROR), inclusive = true
        )
    }

    override fun setContinueButtonState(state: PhoneNumberScreenContinueButtonState) {
        with(binding.buttonConfirmPhone) {
            when (state) {
                PhoneNumberScreenContinueButtonState.ENABLED_TO_CONTINUE -> {
                    setBackgroundColor(getColor(R.color.night))
                    setTextColor(getColor(R.color.lime))
                    setText(R.string.common_continue)
                    setIconResource(R.drawable.ic_arrow_forward)
                    isEnabled = true
                }
                PhoneNumberScreenContinueButtonState.DISABLED_INPUT_IS_EMPTY -> {
                    setBackgroundColor(getColor(R.color.rain))
                    setTextColor(getColor(R.color.mountain))
                    setText(R.string.onboarding_fill_your_number)
                    icon = null
                    isEnabled = false
                }
            }
        }
    }

    private fun setOnResultListener() {
        childFragmentManager.setFragmentResultListener(REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            bundle.getParcelable<CountryCode>(RESULT_KEY)?.let {
                presenter.onCountryCodeChanged(it)
            }
        }
    }

    override fun showSmsDeliveryFailedForNumber() {
        binding.editTextPhoneNumber.showError(getString(R.string.onboarding_no_sms_for_number))
    }

    private fun onCountryCodeChanged(countryCode: String) {
        presenter.onCountryCodeChanged(countryCode)
    }

    private fun onPhoneChanged(phone: String) {
        presenter.onPhoneChanged(phone)
    }

    private fun onCountryClickListener() {
        presenter.onCountryCodeInputClicked()
    }

    override fun showErrorMessage(messageResId: Int) {
        showErrorSnackBar(getString(messageResId))
    }

    override fun showErrorMessage(e: Throwable?) {
        e?.message?.let { showErrorSnackBar(message = it) }
    }
}
