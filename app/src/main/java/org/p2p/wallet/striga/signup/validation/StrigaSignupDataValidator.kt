package org.p2p.wallet.striga.signup.validation

import org.p2p.core.common.TextContainer
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignupDataValidator {
    private val validators = mutableMapOf(
        StrigaSignupDataType.PHONE_NUMBER to listOf(PhoneNumberInputValidator()),
        StrigaSignupDataType.FIRST_NAME to listOf(LengthInputValidator(2..40)),
        StrigaSignupDataType.LAST_NAME to listOf(LengthInputValidator(2..40)),
        StrigaSignupDataType.DATE_OF_BIRTH to listOf(BirthdayInputValidator()),

        StrigaSignupDataType.CITY to listOf(LengthInputValidator(1..40)),
        StrigaSignupDataType.CITY_ADDRESS_LINE to listOf(LengthInputValidator(1..80)),
        StrigaSignupDataType.CITY_POSTAL_CODE to listOf(LengthInputValidator(1..20)),
        StrigaSignupDataType.CITY_STATE to listOf(LengthInputValidator(1..20)),
    )
    private val allFieldsValidator = EmptyInputValidator()

    fun validate(data: StrigaSignupData): StrigaSignupFieldState {
        val fieldValue = data.value?.trim()

        // all fields must be not empty and not null
        if (!allFieldsValidator.validate(fieldValue)) {
            return data.toUiField(false, allFieldsValidator.errorMessage)
        }

        // find validator for specific field
        // if no validators found, then field is valid
        val validators = validators[data.type] ?: return data.toUiField(true)

        var firstError: TextContainer? = null
        val isValid = validators.all { validator ->
            validator.validate(fieldValue).also { isValid ->
                if (!isValid && firstError == null) {
                    firstError = validator.errorMessage
                }
            }
        }

        return data.toUiField(
            isValid = isValid,
            errorMessage = firstError
        )
    }

    private fun StrigaSignupData.toUiField(
        isValid: Boolean,
        errorMessage: TextContainer? = null
    ): StrigaSignupFieldState {
        return StrigaSignupFieldState(
            fieldValue = value.orEmpty(),
            type = type,
            isValid = isValid,
            errorMessage = errorMessage
        )
    }
}
