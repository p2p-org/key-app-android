package org.p2p.wallet.striga.ui.firststep

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitEditText
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaSignUpFirstStepBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.model.StrigaSignupDataType
import org.p2p.wallet.striga.ui.secondstep.StrigaSignUpSecondStepFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaSignUpFirstStepFragment :
    BaseMvpFragment<StrigaSignUpFirstStepContract.View, StrigaSignUpFirstStepContract.Presenter>(
        R.layout.fragment_striga_sign_up_first_step
    ),
    StrigaSignUpFirstStepContract.View {

    companion object {
        fun create() = StrigaSignUpFirstStepFragment()
    }

    override val presenter: StrigaSignUpFirstStepContract.Presenter by inject()
    private val binding: FragmentStrigaSignUpFirstStepBinding by viewBinding()

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

            StrigaSignupDataType.cachedValues.forEach { dataType ->
                val view = editTextFieldsMap[dataType] ?: return@forEach
                view.addOnTextChangedListener { editable ->
                    presenter.onFieldChanged(newValue = editable.toString(), type = dataType)
                }
            }
            buttonNext.setOnClickListener {
                replaceFragment(StrigaSignUpSecondStepFragment.create())
            }
        }
    }

    override fun updateSignupField(newValue: String, type: StrigaSignupDataType) {
        val view = editTextFieldsMap[type]
        view?.setText(newValue)
    }

    private fun createEditTextsMap(): Map<StrigaSignupDataType, UiKitEditText> {
        return with(binding) {
            buildMap {
                put(StrigaSignupDataType.EMAIL, editTextEmail)
                editTextEmail.setViewTag(StrigaSignupDataType.EMAIL)

                put(StrigaSignupDataType.PHONE_NUMBER, editTextPhoneNumber)
                editTextPhoneNumber.setViewTag(StrigaSignupDataType.PHONE_NUMBER)

                put(StrigaSignupDataType.FIRST_NAME, editTextFirstName)
                editTextFirstName.setViewTag(StrigaSignupDataType.FIRST_NAME)

                put(StrigaSignupDataType.LAST_NAME, editTextLastname)
                editTextLastname.setViewTag(StrigaSignupDataType.LAST_NAME)

                put(StrigaSignupDataType.DATE_OF_BIRTH, editTextBirthday)
                editTextBirthday.setViewTag(StrigaSignupDataType.DATE_OF_BIRTH)

                put(StrigaSignupDataType.COUNTRY, editTextCountry)
                editTextCountry.setViewTag(StrigaSignupDataType.COUNTRY)
            }
        }
    }
}
