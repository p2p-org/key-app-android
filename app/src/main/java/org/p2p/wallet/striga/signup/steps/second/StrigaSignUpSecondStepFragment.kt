package org.p2p.wallet.striga.signup.steps.second

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitEditText
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaSignUpSecondStepBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.striga.signup.finish.StrigaSignupFinishFragment
import org.p2p.wallet.striga.signup.presetpicker.StrigaPresetDataPickerFragment
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaOccupation
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.toDp
import org.p2p.wallet.utils.viewbinding.getDrawable
import org.p2p.wallet.utils.viewbinding.viewBinding

private typealias IView = StrigaSignUpSecondStepContract.View
private typealias IPresenter = StrigaSignUpSecondStepContract.Presenter

class StrigaSignUpSecondStepFragment :
    BaseMvpFragment<IView, IPresenter>(R.layout.fragment_striga_sign_up_second_step),
    IView {

    companion object {
        private const val FUNDS_REQUEST_KEY = "FUNDS_REQUEST_KEY"
        private const val OCCUPATION_REQUEST_KEY = "OCCUPATION_REQUEST_KEY"
        private const val COUNTRY_REQUEST_KEY = "COUNTRY_REQUEST_KEY"
        private const val SELECTED_ITEM_RESULT_KEY = "SELECTED_ITEM_RESULT_KEY"

        fun create() = StrigaSignUpSecondStepFragment()
    }

    override val presenter: IPresenter by inject()
    private val binding: FragmentStrigaSignUpSecondStepBinding by viewBinding()
    private lateinit var editTextFieldsMap: Map<StrigaSignupDataType, UiKitEditText>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        editTextFieldsMap = createEditTextsMap()
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

            StrigaSignupDataType.values().forEach { dataType ->
                val inputView = editTextFieldsMap[dataType] ?: return@forEach
                inputView.addOnTextChangedListener { editable ->
                    presenter.onFieldChanged(type = dataType, newValue = editable.toString())
                }
            }

            editTextOccupation.setOnClickListener {
                presenter.onOccupationClicked()
            }
            editTextFunds.setOnClickListener {
                presenter.onFundsClicked()
            }
            editTextCountry.setOnClickListener {
                presenter.onCountryClicked()
            }

            buttonNext.setOnClickListener {
                presenter.onSubmit()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.saveChanges()
    }

    override fun updateSignupField(type: StrigaSignupDataType, newValue: String) {
        val view = editTextFieldsMap[type]
        view?.setText(newValue)
    }

    override fun navigateNext() {
        replaceFragment(
            SmsInputFactory.create(
                type = SmsInputFactory.Type.StrigaSignup,
                destinationFragment = StrigaSignupFinishFragment::class.java,
            )
        )
    }

    override fun navigateToPhoneError() {
        replaceFragment(StrigaSignupErrorNumberAlreadyUsedFragment.create())
    }

    override fun setErrors(errors: List<StrigaSignupFieldState>) {
        errors.forEach {
            editTextFieldsMap[it.type]?.bindError(it.errorMessage)
        }
    }

    override fun clearErrors() {
        editTextFieldsMap.values.forEach { it.bindError(null) }
    }

    override fun clearError(type: StrigaSignupDataType) {
        editTextFieldsMap[type]?.bindError(null)
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

    override fun setProgressIsVisible(isVisible: Boolean) {
        with(binding.buttonNext) {
            isClickable = !isVisible
            setLoading(isVisible)
        }
    }

    override fun scrollToFirstError(type: StrigaSignupDataType) {
        editTextFieldsMap[type]?.let {
            binding.containerScroll.post {
                binding.containerScroll.scrollTo(0, it.top - 32.toDp())
            }
        }
    }

    override fun showSourceOfFundsPicker(selectedItem: StrigaSourceOfFunds?) {
        replaceFragmentForResult(
            target = StrigaPresetDataPickerFragment.create(
                requestKey = FUNDS_REQUEST_KEY,
                resultKey = SELECTED_ITEM_RESULT_KEY,
                dataToPick = StrigaPresetDataItem.SourceOfFunds(selectedItem)
            ),
            requestKey = FUNDS_REQUEST_KEY,
            onResult = ::onFragmentResult
        )
    }

    override fun showOccupationPicker(selectedItem: StrigaOccupation?) {
        replaceFragmentForResult(
            target = StrigaPresetDataPickerFragment.create(
                requestKey = OCCUPATION_REQUEST_KEY,
                resultKey = SELECTED_ITEM_RESULT_KEY,
                dataToPick = StrigaPresetDataItem.Occupation(selectedItem)
            ),
            requestKey = OCCUPATION_REQUEST_KEY,
            onResult = ::onFragmentResult
        )
    }

    override fun showCurrentCountryPicker(selectedItem: CountryCode?) {
        replaceFragmentForResult(
            target = StrigaPresetDataPickerFragment.create(
                requestKey = COUNTRY_REQUEST_KEY,
                resultKey = SELECTED_ITEM_RESULT_KEY,
                dataToPick = StrigaPresetDataItem.Country(selectedItem)
            ),
            requestKey = COUNTRY_REQUEST_KEY,
            onResult = ::onFragmentResult
        )
    }

    private fun onFragmentResult(requestKey: String, bundle: Bundle) {
        when (requestKey) {
            OCCUPATION_REQUEST_KEY -> {
                val selectedItem =
                    bundle.getParcelableCompat<StrigaPresetDataItem.Occupation>(SELECTED_ITEM_RESULT_KEY)
                        ?: return
                presenter.onPresetDataChanged(selectedItem)
            }
            FUNDS_REQUEST_KEY -> {
                val selectedItem =
                    bundle.getParcelableCompat<StrigaPresetDataItem.SourceOfFunds>(SELECTED_ITEM_RESULT_KEY)
                        ?: return
                presenter.onPresetDataChanged(selectedItem)
            }
            COUNTRY_REQUEST_KEY -> {
                val selectedItem =
                    bundle.getParcelableCompat<StrigaPresetDataItem.Country>(SELECTED_ITEM_RESULT_KEY)
                        ?: return
                presenter.onPresetDataChanged(selectedItem)
            }
            else -> throw IllegalStateException("Result for $requestKey is unhandled: ")
        }
    }

    private fun createEditTextsMap(): Map<StrigaSignupDataType, UiKitEditText> {
        return with(binding) {
            buildMap {
                StrigaSignupDataType.OCCUPATION.let {
                    this[it] = editTextOccupation
                    editTextOccupation.setViewTag(it)
                }
                StrigaSignupDataType.SOURCE_OF_FUNDS.let {
                    this[it] = editTextFunds
                    editTextFunds.setViewTag(it)
                }
                StrigaSignupDataType.COUNTRY_ALPHA_2.let {
                    this[it] = editTextCountry
                    editTextCountry.setViewTag(it)
                }
                StrigaSignupDataType.CITY.let {
                    this[it] = editTextCity
                    editTextCity.setViewTag(it)
                }
                StrigaSignupDataType.CITY_ADDRESS_LINE.let {
                    this[it] = editTextAddressLine
                    editTextAddressLine.setViewTag(it)
                }
                StrigaSignupDataType.CITY_POSTAL_CODE.let {
                    this[it] = editTextPostalCode
                    editTextPostalCode.setViewTag(it)
                }
                StrigaSignupDataType.CITY_STATE.let {
                    this[it] = editTextStateOrRegion
                    editTextStateOrRegion.setViewTag(it)
                }
            }
        }
    }
}
