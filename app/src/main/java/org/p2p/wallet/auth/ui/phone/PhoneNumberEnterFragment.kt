package org.p2p.wallet.auth.ui.phone

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.hideKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.generalerror.GeneralErrorScreenError
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorFragment
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerFragment
import org.p2p.wallet.auth.ui.phone.countrypicker.CountryCodePickerFragment
import org.p2p.wallet.auth.ui.restore.common.CommonRestoreFragment
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentPhoneNumberEnterBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
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

        fun create() = PhoneNumberEnterFragment()
    }

    override val presenter: PhoneNumberEnterContract.Presenter by inject()

    private val binding: FragmentPhoneNumberEnterBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.initViews()
        setOnResultListener()
    }

    private fun FragmentPhoneNumberEnterBinding.initViews() {
        toolbar.setNavigationOnClickListener {
            popBackStack()
        }

        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.helpItem) {
                view?.hideKeyboard()
                IntercomService.showMessenger()
                true
            } else {
                false
            }
        }

        buttonConfirmPhone.setOnClickListener {
            presenter.submitUserPhoneNumber(editTextPhoneNumber.text?.toString().orEmpty())
        }
    }

    override fun initCreateWalletViews() {
        binding.toolbar.setNavigationOnClickListener(null)
        binding.toolbar.navigationIcon = null
        binding.textViewSubtitle.setText(R.string.onboarding_add_number_message)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    override fun initRestoreWalletViews() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
        binding.textViewSubtitle.setText(R.string.onboarding_restore_number_message)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popAndReplaceFragment(CommonRestoreFragment.create(), inclusive = true)
        }
    }

    override fun showDefaultCountryCode(defaultCountryCode: CountryCode?) {
        binding.editTextPhoneNumber.setupViewState(
            countryCode = defaultCountryCode,
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
        addFragment(CountryCodePickerFragment.create(selectedCountryCode, REQUEST_KEY, RESULT_KEY))
    }

    override fun navigateToSmsInput() {
        replaceFragment(NewSmsInputFragment.create())
    }

    override fun navigateToAccountBlocked(cooldownTtl: Long) {
        replaceFragment(
            OnboardingGeneralErrorTimerFragment.create(
                error = GeneralErrorTimerScreenError.BLOCK_PHONE_NUMBER_ENTER,
                timerLeftTime = cooldownTtl
            )
        )
    }

    override fun navigateToCriticalErrorScreen(error: GeneralErrorScreenError) {
        popAndReplaceFragment(OnboardingGeneralErrorFragment.create(error), inclusive = true)
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
        requireActivity().supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            bundle.getParcelable<CountryCode>(RESULT_KEY)?.let { presenter.onCountryCodeChanged(it) }
        }
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
}
