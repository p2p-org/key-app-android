package org.p2p.wallet.striga.signup.ui

import androidx.activity.addCallback
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.components.UiKitEditText
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.SimpleMaskFormatter
import org.p2p.wallet.databinding.FragmentStrigaSignUpFirstStepBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataPickerFragment
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataToPick
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.toDp
import org.p2p.wallet.utils.viewbinding.getDrawable
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

    private lateinit var editTextFieldsMap: Map<StrigaSignupDataType, UiKitEditText>

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
                inputView.addOnTextChangedListener { editable ->
                    presenter.onFieldChanged(newValue = editable.toString(), type = dataType)
                }
            }
            editTextCountry.setOnClickListener {
                showCountryOfBirthPicker()
            }
            buttonNext.setOnClickListener {
                presenter.onSubmit()
            }

            binding.editTextBirthday.input.addTextChangedListener(
                birthdayMaskFormatter.textWatcher(
                    binding.editTextBirthday.input
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.saveChanges()
    }

    override fun setPhoneMask(phoneMask: String?) {
        if (phoneMask != null) {
            val formatter = SimpleMaskFormatter(phoneMask)
            binding.editTextPhoneNumber.input.addTextChangedListener(
                formatter.textWatcher(
                    binding.editTextPhoneNumber.input
                )
            )
        }
    }

    private fun showCountryOfBirthPicker() {
        replaceFragment(StrigaPresetDataPickerFragment.create(dataToPick = StrigaPresetDataToPick.COUNTRY_OF_BIRTH))
    }

    override fun updateSignupField(type: StrigaSignupDataType, newValue: String) {
        val view = editTextFieldsMap[type]
        view?.setText(newValue)
    }

    override fun navigateNext() {
        replaceFragment(StrigaSignUpSecondStepFragment.create())
    }

    override fun setErrors(errors: List<StrigaSignupFieldState>) {
        errors.forEach {
            editTextFieldsMap[it.type]?.bindError(it.errorMessage)
        }
    }

    override fun scrollToFirstError(type: StrigaSignupDataType) {
        editTextFieldsMap[type]?.let {
            binding.containerScroll.post {
                binding.containerScroll.scrollTo(0, it.top - 32.toDp())
            }
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

    private fun createEditTextsMap(): Map<StrigaSignupDataType, UiKitEditText> {
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
                StrigaSignupDataType.COUNTRY_OF_BIRTH.let {
                    this[it] = editTextCountry
                    editTextCountry.setViewTag(it)
                }
            }
        }
    }

    private fun onBackPressed() {
        popBackStack()
    }
}
