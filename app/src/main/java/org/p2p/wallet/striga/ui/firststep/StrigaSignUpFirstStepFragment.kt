package org.p2p.wallet.striga.ui.firststep

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitEditText
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaSignUpFirstStepBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.ui.countrypicker.StrigaCountryPickerFragment
import org.p2p.wallet.striga.ui.secondstep.StrigaSignUpSecondStepFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaSignUpFirstStepFragment :
    BaseMvpFragment<StrigaSignUpFirstStepContract.View, StrigaSignUpFirstStepContract.Presenter>(
        R.layout.fragment_striga_sign_up_first_step
    ),
    StrigaSignUpFirstStepContract.View {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
        const val RESULT_KEY = "RESULT_KEY"
        fun create() = StrigaSignUpFirstStepFragment()
    }

    override val presenter: StrigaSignUpFirstStepContract.Presenter by inject()
    private val binding: FragmentStrigaSignUpFirstStepBinding by viewBinding()

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

            StrigaSignupDataType.cachedValues.forEach { dataType ->
                val view = editTextFieldsMap[dataType] ?: return@forEach
                view.addOnTextChangedListener { editable ->
                    presenter.onFieldChanged(newValue = editable.toString(), type = dataType)
                }
            }

            editTextCountry.setOnClickListener {
                presenter.onCountryClicked()
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

    override fun showCountryPicker(selectedCountry: Country?) {
        replaceFragmentForResult(
            target = StrigaCountryPickerFragment.create(
                selectedCountry = selectedCountry,
                requestKey = REQUEST_KEY,
                resultKey = RESULT_KEY
            ),
            requestKey = REQUEST_KEY,
            onResult = ::onFragmentResult
        )
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

    private fun onFragmentResult(requestKey: String, bundle: Bundle) {
        if (requestKey != REQUEST_KEY) return
        val selectedCountry = bundle.getParcelable(RESULT_KEY) as? Country
        presenter.onCountryChanged(selectedCountry ?: return)
    }
}
