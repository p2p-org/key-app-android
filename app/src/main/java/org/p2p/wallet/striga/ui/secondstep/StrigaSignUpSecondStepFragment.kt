package org.p2p.wallet.striga.ui.secondstep

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitEditText
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaSignUpSecondStepBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaPickerItem
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.ui.countrypicker.StrigaPresetDataPickerFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.viewbinding.viewBinding

typealias IView = StrigaSignUpSecondStepContract.View
typealias IPresenter = StrigaSignUpSecondStepContract.Presenter

class StrigaSignUpSecondStepFragment :
    BaseMvpFragment<IView, IPresenter>(R.layout.fragment_striga_sign_up_second_step),
    IView {

    companion object {
        const val FUNDS_REQUEST_KEY = "FUNDS_REQUEST_KEY"
        const val FUNDS_RESULT_KEY = "FUNDS_RESULT_KEY"

        const val OCCUPATION_REQUEST_KEY = "OCCUPATION_REQUEST_KEY"
        const val OCCUPATION_RESULT_KEY = "OCCUPATION_RESULT_KEY"
        fun create() = StrigaSignUpSecondStepFragment()
    }

    override val presenter: IPresenter by inject()
    private val binding: FragmentStrigaSignUpSecondStepBinding by viewBinding()
    private lateinit var editTextFieldsMap: Map<StrigaSignupDataType, UiKitEditText>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTextFieldsMap = createEditTextsMap()
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

            StrigaSignupDataType.cachedValues.forEach { dataType ->
                val view = editTextFieldsMap[dataType] ?: return@forEach
                view.addOnTextChangedListener { editable ->
                    presenter.onFieldChanged(newValue = editable.toString(), type = dataType)
                }
            }
            editTextOccupation.setOnClickListener {
                presenter.onOccupationClicked()
            }
            editTextFunds.setOnClickListener {
                presenter.onSourceOfFundsClicked()
            }
        }
    }

    override fun updateSignupField(newValue: String, type: StrigaSignupDataType) {
        val view = editTextFieldsMap[type]
        view?.setText(newValue)
    }

    override fun showOccupationPicker(selectedValue: StrigaOccupation?) {
        replaceFragmentForResult(
            target = StrigaPresetDataPickerFragment.create(
                selectedCountry = StrigaPickerItem.OccupationItem(selectedValue),
                requestKey = OCCUPATION_REQUEST_KEY,
                resultKey = OCCUPATION_RESULT_KEY
            ),
            requestKey = OCCUPATION_REQUEST_KEY,
            onResult = ::onFragmentResult
        )
    }

    override fun showFundsPicker(selectedValue: StrigaSourceOfFunds?) {
        replaceFragmentForResult(
            target = StrigaPresetDataPickerFragment.create(
                selectedCountry = StrigaPickerItem.FundsItem(selectedValue),
                requestKey = FUNDS_REQUEST_KEY,
                resultKey = FUNDS_RESULT_KEY
            ),
            requestKey = FUNDS_REQUEST_KEY,
            onResult = ::onFragmentResult
        )
    }

    private fun createEditTextsMap(): Map<StrigaSignupDataType, UiKitEditText> {
        return with(binding) {
            buildMap {
                put(StrigaSignupDataType.OCCUPATION, editTextOccupation)
                editTextOccupation.setViewTag(StrigaSignupDataType.OCCUPATION)

                put(StrigaSignupDataType.SOURCE_OF_FUNDS, editTextFunds)
                editTextFunds.setViewTag(StrigaSignupDataType.SOURCE_OF_FUNDS)

                put(StrigaSignupDataType.COUNTRY, editTextCountry)
                editTextCountry.setViewTag(StrigaSignupDataType.COUNTRY)

                put(StrigaSignupDataType.CITY, editTextCity)
                editTextCity.setViewTag(StrigaSignupDataType.CITY)

                put(StrigaSignupDataType.CITY_ADDRESS_LINE, editTextAddressLine)
                editTextAddressLine.setViewTag(StrigaSignupDataType.CITY_ADDRESS_LINE)

                put(StrigaSignupDataType.CITY_POSTAL_CODE, editTextPostalCode)
                editTextPostalCode.setViewTag(StrigaSignupDataType.CITY_POSTAL_CODE)

                put(StrigaSignupDataType.CITY_STATE, editTextStateOrRegion)
                editTextStateOrRegion.setViewTag(StrigaSignupDataType.CITY_STATE)
            }
        }
    }

    private fun onFragmentResult(requestKey: String, bundle: Bundle) {
        if (bundle.containsKey(OCCUPATION_RESULT_KEY)) {
            val value = bundle.getParcelable(OCCUPATION_RESULT_KEY) as? StrigaPickerItem.OccupationItem
            presenter.onOccupationChanged(value?.selectedItem ?: return)
        }
        if (bundle.containsKey(FUNDS_RESULT_KEY)) {
            val selectedItem = bundle.getParcelable(FUNDS_RESULT_KEY) as? StrigaPickerItem.FundsItem
            presenter.onSourceOfFundsChanged(selectedItem?.selectedItem ?: return)
        }
    }
}
