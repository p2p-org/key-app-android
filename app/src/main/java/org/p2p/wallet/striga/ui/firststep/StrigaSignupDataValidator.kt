package org.p2p.wallet.striga.ui.firststep

import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.utils.DateTimeUtils

class StrigaSignupDataValidator {
    private val localDateFormatter = DateTimeFormatter.ofPattern(DateTimeUtils.PATTERN_DD_MM_YYYY)
        .withResolverStyle ( ResolverStyle.SMART )

    fun validate(data: StrigaSignupData): StrigaSignupField {
        if (data.value.isNullOrEmpty()) {
            // always true if field is empty in start
            return data.toUiField(isValid = true)
        }

        val fieldValue: String = data.value.trim()
        val isFieldValid = when (data.type) {
            // fetched from metadata, so it's always valid
            StrigaSignupDataType.EMAIL -> true
            StrigaSignupDataType.PHONE_NUMBER -> validatePhoneNumber(fieldValue)
            StrigaSignupDataType.FIRST_NAME, StrigaSignupDataType.LAST_NAME -> validateName(fieldValue)
            StrigaSignupDataType.DATE_OF_BIRTH -> validateDateOfBirth(fieldValue)

            // always true, it's a preset data
            StrigaSignupDataType.COUNTRY_OF_BIRTH,
            StrigaSignupDataType.OCCUPATION,
            StrigaSignupDataType.SOURCE_OF_FUNDS,
            StrigaSignupDataType.COUNTRY -> true

            // no need to validate
            StrigaSignupDataType.CITY,
            StrigaSignupDataType.CITY_ADDRESS_LINE,
            StrigaSignupDataType.CITY_POSTAL_CODE,
            StrigaSignupDataType.CITY_STATE -> true
        }
        return data.toUiField(isValid = isFieldValid)
    }

    @Language("RegExp")
    private fun validatePhoneNumber(phoneNumberValue: String): Boolean {
        return Regex("^\\+[0-9]{11,12}\$").matches(phoneNumberValue)
    }

    @Language("RegExp")
    private fun validateName(firstOrLastName: String): Boolean {
        // first of last name can be complex like Ibragim Malahala
        val splitName = firstOrLastName.split(" ") // [Ibragim, Mahalala]
        val nameRegEx = Regex("^[a-zA-Z]{2,40}$")
        return splitName.all(nameRegEx::matches)
    }

    private fun validateDateOfBirth(rawDate: String): Boolean {
        val parsedDateOfBirth = parseDateOrNull(rawDate)
        return parsedDateOfBirth != null &&
            parsedDateOfBirth.isAfter(LocalDate.ofYearDay(1920, 1)) &&
            parsedDateOfBirth.isBefore(LocalDate.ofYearDay(2015, 1))
    }

    fun validateOnConfirm(value: StrigaSignupData) {
    }

    private fun StrigaSignupData.toUiField(isValid: Boolean): StrigaSignupField {
        return StrigaSignupField(
            fieldValue = value.orEmpty(),
            type = type,
            isValid = isValid
        )
    }

    private fun parseDateOrNull(value: String): LocalDate? {
        return kotlin.runCatching { LocalDate.parse(value, localDateFormatter) }
            .getOrNull()
    }
}
