package org.p2p.wallet.striga.signup.ui

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitEditText
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaSignUpSecondStepBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.toDp
import org.p2p.wallet.utils.viewbinding.getDrawable
import org.p2p.wallet.utils.viewbinding.viewBinding

private typealias IView = StrigaSignUpSecondStepContract.View
private typealias IPresenter = StrigaSignUpSecondStepContract.Presenter

class StrigaSignUpSecondStepFragment :
    BaseMvpFragment<IView, IPresenter>(R.layout.fragment_striga_sign_up_second_step),
    IView {

    companion object {
        fun create() = StrigaSignUpSecondStepFragment()
    }

    override val presenter: IPresenter by inject()
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
                val inputView = editTextFieldsMap[dataType] ?: return@forEach
                inputView.addOnTextChangedListener { editable ->
                    presenter.onFieldChanged(newValue = editable.toString(), type = dataType)
                }
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
    }

    override fun setErrors(errors: List<StrigaSignupFieldState>) {
        errors.forEach {
            editTextFieldsMap[it.type]?.bindError(it.errorMessage)
        }
    }

    override fun clearErrors() {
        editTextFieldsMap.values.forEach { it.bindError(null) }
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

    override fun scrollToFirstError(type: StrigaSignupDataType) {
        editTextFieldsMap[type]?.let {
            binding.containerScroll.post {
                binding.containerScroll.scrollTo(0, it.top - 32.toDp())
            }
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
                StrigaSignupDataType.COUNTRY.let {
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
