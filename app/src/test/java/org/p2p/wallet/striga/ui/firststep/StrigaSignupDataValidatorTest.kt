package org.p2p.wallet.striga.ui.firststep

import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import assertk.assertions.matchesPredicate
import assertk.assertions.prop
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignupDataValidatorTest {

    private var validator = StrigaSignupDataValidator()

    @BeforeEach
    fun beforeEach() {
        validator = StrigaSignupDataValidator()
    }

    @Test
    fun `GIVEN all valid data WHEN validate THEN all fields valid`() {
        // GIVEN
        val validData = createValidatedFields()

        // WHEN
        val actualValidatedData = validData.map(validator::validate)

        // THEN
        assertThat(actualValidatedData).all {
            isNotEmpty()
            each {
                it.prop(StrigaSignupField::isValid).isTrue()
                it.prop(StrigaSignupField::fieldValue).isNotEmpty()
            }
        }
    }

    @Test
    fun `GIVEN invalid phone number WHEN validate THEN phone number field is not valid`() {
        // GIVEN
        val validData = createValidatedFields(StrigaSignupDataType.PHONE_NUMBER, "+7123")

        // WHEN
        val actualValidatedData = validData.map(validator::validate)

        // THEN

    }

    @Test
    fun `GIVEN invalid date of birth WHEN validate THEN date of birth field is not valid`() {
        // GIVEN
        val invalidDataWrongFormat = createValidatedFields(StrigaSignupDataType.DATE_OF_BIRTH, "11.2")
        val invalidDataWrongMonth = createValidatedFields(StrigaSignupDataType.DATE_OF_BIRTH, "11.22.2033")
        val invalidDataWrongDay = createValidatedFields(StrigaSignupDataType.DATE_OF_BIRTH, "31.02.2022")
        val invalidDataWrongYear = createValidatedFields(StrigaSignupDataType.DATE_OF_BIRTH, "01.02.1950")

        // WHEN
        val actualValidatedData = listOf(
            invalidDataWrongFormat.map { validator.validate(it) },
            invalidDataWrongMonth.map { validator.validate(it) },
            invalidDataWrongDay.map { validator.validate(it) },
            invalidDataWrongYear.map { validator.validate(it) }
        )

        actualValidatedData.forEach { fieldsWithInvalidDate ->
            assertThat(fieldsWithInvalidDate).all {
                isNotEmpty()
                each { field ->
                    field.matchesPredicate {
                        if (it.type == StrigaSignupDataType.EMAIL) !it.isValid
                        else it.isValid
                    }
                }
            }
        }

        // THEN
        assertThat(actualValidatedData).all {
            isNotEmpty()
            each { field ->
                field.all {
                    isNotEmpty()
                    each {
                        it.matchesPredicate {
                            if (it.type == StrigaSignupDataType.DATE_OF_BIRTH) !it.isValid
                            else it.isValid
                        }
                    }
                }
            }
        }
    }

    private fun createValidatedFields(
        typeToChange: StrigaSignupDataType? = null,
        updateValue: String? = null
    ): List<StrigaSignupData> {
        val validFields = StrigaSignupDataType.cachedValues.map { type ->
            when (type) {
                StrigaSignupDataType.EMAIL -> "example@mail.com"
                StrigaSignupDataType.PHONE_NUMBER -> "+712345678910"
                StrigaSignupDataType.FIRST_NAME -> "Complex first name"
                StrigaSignupDataType.LAST_NAME -> "Complex last name"
                StrigaSignupDataType.DATE_OF_BIRTH -> "11.01.2000"
                StrigaSignupDataType.COUNTRY_OF_BIRTH -> "country"
                StrigaSignupDataType.OCCUPATION -> "occupation"
                StrigaSignupDataType.SOURCE_OF_FUNDS -> "source of funds"
                StrigaSignupDataType.COUNTRY -> "country"
                StrigaSignupDataType.CITY -> "city"
                StrigaSignupDataType.CITY_ADDRESS_LINE -> "city address line"
                StrigaSignupDataType.CITY_POSTAL_CODE -> "postal code"
                StrigaSignupDataType.CITY_STATE -> "city state"
            }
                .let { StrigaSignupData(type = type, value = it) }
        }

        return if (typeToChange != null) {
            requireNotNull(updateValue) { "With provided type should be provided value to change" }
            validFields.map {
                if (it.type == typeToChange) {
                    it.copy(value = updateValue)
                } else {
                    it
                }
            }
        } else {
            validFields
        }
    }
}
