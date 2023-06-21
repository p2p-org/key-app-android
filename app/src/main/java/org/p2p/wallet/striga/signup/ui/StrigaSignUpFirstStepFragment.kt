package org.p2p.wallet.striga.signup.ui

import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitEditText
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.phone.countrypicker.CountryCodePickerFragment
import org.p2p.wallet.auth.widget.PhoneNumberInputView
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.SimpleMaskFormatter
import org.p2p.wallet.databinding.FragmentStrigaSignUpFirstStepBinding
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataPickerFragment
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.toDp
import org.p2p.wallet.utils.viewbinding.getDrawable
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaSignUpFirstStepFragment :
    BaseMvpFragment<StrigaSignUpFirstStepContract.View, StrigaSignUpFirstStepContract.Presenter>(
        R.layout.fragment_striga_sign_up_first_step
    ),
    StrigaSignUpFirstStepContract.View {

    companion object {
        const val REQUEST_KEY_COUNTRY = "REQUEST_KEY_COUNTRY"
        const val RESULT_KEY_COUNTRY = "RESULT_KEY_COUNTRY"
        const val REQUEST_KEY_PHONE_COUNTRY_CODE = "REQUEST_KEY_COUNTRY_CODE"
        const val RESULT_KEY_PHONE_COUNTRY_CODE = "RESULT_CODE_COUNTRY_CODE"
        const val ARG_SCROLL_TO_VIEW_ID = "ARG_SCROLL_TO_VIEW_ID"

        fun create(@IdRes scrollToViewId: Int = View.NO_ID) = StrigaSignUpFirstStepFragment().apply {
            arguments = bundleOf(
                ARG_SCROLL_TO_VIEW_ID to scrollToViewId
            )
        }
    }

    override val presenter: StrigaSignUpFirstStepContract.Presenter by inject()
    private val inAppFeatureFlags: InAppFeatureFlags by inject()

    private val binding: FragmentStrigaSignUpFirstStepBinding by viewBinding()
    private val scrollToViewId: Int by args(ARG_SCROLL_TO_VIEW_ID)

    private lateinit var editTextFieldsMap: Map<StrigaSignupDataType, View>

    private var birthdayMaskFormatter = SimpleMaskFormatter("##.##.####")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        editTextFieldsMap = createEditTextsMap()
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
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

            StrigaSignupDataType.values().forEach { dataType ->
                val inputView = editTextFieldsMap[dataType] ?: return@forEach
                if (inputView is UiKitEditText) {
                    inputView.addOnTextChangedListener { editable ->
                        presenter.onFieldChanged(type = dataType, newValue = editable.toString())
                    }
                }
            }
            editTextCountry.setOnClickListener {
                presenter.onCountryOfBirthClicked()
            }
            buttonNext.setOnClickListener {
                presenter.onSubmit()
            }

            binding.editTextBirthday.input.addTextChangedListener(
                birthdayMaskFormatter.textWatcher(
                    binding.editTextBirthday.input
                )
            )
            setFragmentResultListener(
                REQUEST_KEY_PHONE_COUNTRY_CODE,
                ::onFragmentResult
            )
            setFragmentResultListener(
                REQUEST_KEY_COUNTRY,
                ::onFragmentResult
            )

            editTextEmail.isEnabled = inAppFeatureFlags.strigaEnableEmailFieldFlag.featureValue
        }

        if (scrollToViewId != View.NO_ID) {
            binding.root.findViewById<View>(scrollToViewId)?.let { targetView ->
                binding.containerScroll.post {
                    binding.containerScroll.scrollTo(0, targetView.top - 32.toDp())
                }
            }
        }
    }

    override fun onStop() {
        presenter.saveChanges()
        super.onStop()
    }

    override fun showCountryOfBirthPicker(countryCode: CountryCode?) {
        replaceFragmentForResult(
            StrigaPresetDataPickerFragment.create(
                dataToPick = StrigaPresetDataItem.Country(countryCode),
                requestKey = REQUEST_KEY_COUNTRY,
                resultKey = RESULT_KEY_COUNTRY
            ),
            requestKey = REQUEST_KEY_COUNTRY,
            onResult = ::onFragmentResult
        )
    }

    override fun setupPhoneCountryCodePicker(selectedCountryCode: CountryCode?, selectedPhoneNumber: String?) {
        binding.editTextPhoneNumber.setupViewState(
            countryCode = selectedCountryCode,
            onPhoneChanged = ::onPhoneChanged,
            savedPhoneNumber = selectedPhoneNumber,
            onCountryClickListener = ::onPhoneCountryClickListener,
            requestFocus = false
        )
    }

    override fun updateSignupField(type: StrigaSignupDataType, newValue: String) {
        val view = editTextFieldsMap[type]
        setText(view = view ?: return, newValue = newValue)
    }

    override fun navigateNext() {
        replaceFragment(StrigaSignUpSecondStepFragment.create())
    }

    override fun setErrors(errors: List<StrigaSignupFieldState>) {
        errors.forEach {
            bindError(
                view = editTextFieldsMap[it.type] ?: return,
                error = it.errorMessage
            )
        }
    }

    override fun clearError(type: StrigaSignupDataType) {
        bindError(view = editTextFieldsMap[type] ?: return, error = null)
    }

    override fun scrollToFirstError(type: StrigaSignupDataType) {
        editTextFieldsMap[type]?.let {
            binding.containerScroll.post {
                binding.containerScroll.scrollTo(0, it.top - 32.toDp())
            }
        }
    }

    override fun clearErrors() {
        editTextFieldsMap.values.forEach { bindError(view = it, error = null) }
    }

    override fun setButtonIsEnabled(isEnabled: Boolean) {
        with(binding.buttonNext) {
            this.isEnabled = isEnabled
            icon = if (isEnabled) {
                bind(TextContainer(R.string.auth_next))
                binding.getDrawable(R.drawable.ic_arrow_right)
            } else {
                bind(TextContainer(R.string.striga_button_error_check_red_fields))
                null
            }
        }
    }

    override fun showPhoneCountryCode(countryCode: CountryCode?) {
        binding.editTextPhoneNumber.updateViewState(countryCode)
    }

    override fun showPhoneCountryCodePicker(selectedCountryCode: CountryCode?) {
        replaceFragmentForResult(
            target = CountryCodePickerFragment.create(
                selectedCountry = selectedCountryCode,
                requestKey = REQUEST_KEY_PHONE_COUNTRY_CODE,
                resultKey = RESULT_KEY_PHONE_COUNTRY_CODE
            ),
            requestKey = REQUEST_KEY_PHONE_COUNTRY_CODE,
            onResult = ::onFragmentResult
        )
    }

    private fun createEditTextsMap(): Map<StrigaSignupDataType, View> {
        return with(binding) {
            buildMap {
                StrigaSignupDataType.EMAIL.let {
                    this[it] = editTextEmail
                    editTextEmail.setViewTag(it)
                }
                StrigaSignupDataType.PHONE_NUMBER.let {
                    this[it] = editTextPhoneNumber
                    editTextPhoneNumber.setViewTag(it)
                }
                StrigaSignupDataType.PHONE_CODE_WITH_PLUS.let {
                    this[it] = editTextPhoneNumber.phoneCodeView
                    editTextPhoneNumber.phoneCodeView.tag = it
                }
                StrigaSignupDataType.FIRST_NAME.let {
                    this[it] = editTextFirstName
                    editTextFirstName.setViewTag(it)
                }
                StrigaSignupDataType.LAST_NAME.let {
                    this[it] = editTextLastname
                    editTextLastname.setViewTag(it)
                }
                StrigaSignupDataType.DATE_OF_BIRTH.let {
                    this[it] = editTextBirthday
                    editTextBirthday.setViewTag(it)
                }
                StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3.let {
                    this[it] = editTextCountry
                    editTextCountry.setViewTag(it)
                }
            }
        }
    }

    private fun onBackPressed() {
        popBackStackTo(MainContainerFragment::class)
    }

    private fun onFragmentResult(requestKey: String, bundle: Bundle) {
        when {
            bundle.containsKey(RESULT_KEY_COUNTRY) -> {
                val selectedCountry = bundle.getParcelableCompat<StrigaPresetDataItem.Country>(RESULT_KEY_COUNTRY)
                presenter.onCountryOfBirthdayChanged(selectedCountry?.details ?: return)
            }
            bundle.containsKey(RESULT_KEY_PHONE_COUNTRY_CODE) -> {
                val countryCode = bundle.getParcelableCompat<CountryCode>(RESULT_KEY_PHONE_COUNTRY_CODE) ?: return
                presenter.onPhoneCountryCodeChanged(countryCode)
            }
        }
    }

    private fun onPhoneChanged(phone: String) {
        presenter.onPhoneNumberChanged(phone)
    }

    private fun onPhoneCountryClickListener() {
        presenter.onPhoneCountryCodeClicked()
    }

    private fun bindError(view: View, error: TextContainer?) {
        if (view is UiKitEditText) {
            view.bindError(error)
        } else if (view is PhoneNumberInputView) {
            view.showError(error)
        }
    }

    private fun setText(view: View, newValue: String) {
        if (view is UiKitEditText) {
            view.setText(newValue)
        } else if (view is PhoneNumberInputView) {
            view.setText(newValue)
        }
    }
}
