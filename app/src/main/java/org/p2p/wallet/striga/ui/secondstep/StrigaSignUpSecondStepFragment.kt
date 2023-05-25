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
import org.p2p.wallet.striga.model.StrigaSignupDataType
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

typealias IV = StrigaSignUpSecondStepContract.View
typealias IP = StrigaSignUpSecondStepContract.Presenter

class StrigaSignUpSecondStepFragment : BaseMvpFragment<IV, IP>(R.layout.fragment_striga_sign_up_second_step), IV {

    companion object {
        fun create() = StrigaSignUpSecondStepFragment()
    }

    override val presenter: IP by inject()
    private val binding: FragmentStrigaSignUpSecondStepBinding by viewBinding()
    private val editTextFieldsMap: Map<StrigaSignupDataType, UiKitEditText> by lazy { createEditTextsMap() }

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

            StrigaSignupDataType.values().forEach { dataType ->
                val view = editTextFieldsMap[dataType] ?: return@forEach
                view.addOnTextChangedListener { editable ->
                    presenter.onTextChanged(newValue = editable.toString(), type = dataType)
                }
            }
        }
    }

    override fun updateText(newValue: String, type: StrigaSignupDataType) {
        val view = editTextFieldsMap[type]
        view?.setText(newValue)
    }

    private fun createEditTextsMap(): Map<StrigaSignupDataType, UiKitEditText> {
        return with(binding) {
            mutableMapOf<StrigaSignupDataType, UiKitEditText>().apply {
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
}
