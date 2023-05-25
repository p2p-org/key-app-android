package org.p2p.wallet.striga.ui.personaldata

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.UiKitEditText
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaPersonalInfoBinding
import org.p2p.wallet.striga.model.StrigaSignupDataType
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaSignUpFirstStepFragment :
    BaseMvpFragment<StrigaPersonalInfoContract.View, StrigaPersonalInfoContract.Presenter>(
        R.layout.fragment_striga_personal_info
    ),
    StrigaPersonalInfoContract.View {

    companion object {
        fun create() = StrigaSignUpFirstStepFragment()
    }

    override val presenter: StrigaPersonalInfoContract.Presenter by inject()
    private val binding: FragmentStrigaPersonalInfoBinding by viewBinding()

    private val editTextFieldsMap: Map<StrigaSignupDataType, UiKitEditText> by lazy { createEditTextsMap() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
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
                put(StrigaSignupDataType.EMAIL, editTextEmail)
                editTextEmail.setViewTag(StrigaSignupDataType.EMAIL)

                put(StrigaSignupDataType.PHONE_NUMBER, editTextPhoneNumber)
                editTextPhoneNumber.setViewTag(StrigaSignupDataType.PHONE_NUMBER)

                put(StrigaSignupDataType.FIRST_NAME, editTextFirstName)
                editTextFirstName.setViewTag(StrigaSignupDataType.FIRST_NAME)

                put(StrigaSignupDataType.LAST_NAME, editTextSurname)
                editTextSurname.setViewTag(StrigaSignupDataType.LAST_NAME)

                put(StrigaSignupDataType.DATE_OF_BIRTH, editTextBirthday)
                editTextBirthday.setViewTag(StrigaSignupDataType.DATE_OF_BIRTH)

                put(StrigaSignupDataType.COUNTRY, editTextCountry)
                editTextCountry.setViewTag(StrigaSignupDataType.COUNTRY)
            }
        }
    }
}
