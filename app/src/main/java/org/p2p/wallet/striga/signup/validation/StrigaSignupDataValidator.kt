package org.p2p.wallet.striga.signup.validation

import org.p2p.core.common.TextContainer
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignupDataValidator {
    private val validators = mutableMapOf(
        StrigaSignupDataType.EMAIL to listOf(EmptyInputValidator()),
        StrigaSignupDataType.PHONE_CODE_WITH_PLUS to listOf(EmptyInputValidator()),
        StrigaSignupDataType.PHONE_NUMBER to listOf(EmptyInputValidator()),

        StrigaSignupDataType.FIRST_NAME to listOf(
            EmptyInputValidator(),
            LengthInputValidator(2..40)
        ),
        StrigaSignupDataType.LAST_NAME to listOf(
            EmptyInputValidator(),
            LengthInputValidator(2..40)
        ),
        StrigaSignupDataType.DATE_OF_BIRTH to listOf(
            EmptyInputValidator(),
            BirthdayInputValidator()
        ),
        StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3 to listOf(
            EmptyInputValidator()
        ),
        StrigaSignupDataType.COUNTRY_ALPHA_2 to listOf(
            EmptyInputValidator(),
        ),
        StrigaSignupDataType.OCCUPATION to listOf(
            EmptyInputValidator()
        ),
        StrigaSignupDataType.SOURCE_OF_FUNDS to listOf(
            EmptyInputValidator()
        ),
        StrigaSignupDataType.CITY to listOf(
            EmptyInputValidator(),
            LengthInputValidator(2..40)
        ),
        StrigaSignupDataType.CITY_ADDRESS_LINE to listOf(
            EmptyInputValidator(),
            LengthInputValidator(1..160)
        ),
        StrigaSignupDataType.CITY_POSTAL_CODE to listOf(
            EmptyInputValidator(),
            LengthInputValidator(1..20)
        ),
        // state is an optional field
        StrigaSignupDataType.CITY_STATE to listOf(
            LengthInputValidator(0..20)
        ),
    )

    fun validate(data: StrigaSignupData): StrigaSignupFieldState {
        val fieldValue = data.value?.trim()

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

    fun setPhoneValidator(validator: InputValidator) {
        validators[StrigaSignupDataType.PHONE_NUMBER] = listOf(
            EmptyInputValidator(),
            validator
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
