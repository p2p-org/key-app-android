package org.p2p.wallet.striga.signup.validation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R
import org.p2p.wallet.utils.DateTimeUtils

class BirthdayInputValidator(
    private val minimumYear: Int = 1920,
    private val maximumYear: Int = 2015,
) : InputValidator {
    override var errorMessage: TextContainer = TextContainer(R.string.striga_validation_error_wrong_birthday_common)

    private val localDateFormatter = DateTimeFormatter.ofPattern(DateTimeUtils.PATTERN_DD_MM_YYYY)
        .withResolverStyle(ResolverStyle.SMART)

    override fun validate(input: String?): Boolean {
        if (input == null) return false

        val parsedDateOfBirth = parseDateOrNull(input)

        return when {
            parsedDateOfBirth == null -> {
                errorMessage = TextContainer(R.string.striga_validation_error_wrong_birthday_common)
                false
            }
            parsedDateOfBirth.isBefore(LocalDate.ofYearDay(minimumYear, 1)) -> {
                errorMessage = TextContainer.ResParams(
                    R.string.striga_validation_error_wrong_birthday_older_than,
                    listOf(minimumYear)
                )
                false
            }
            parsedDateOfBirth.isAfter(LocalDate.ofYearDay(maximumYear, 1)) -> {
                errorMessage = TextContainer.ResParams(
                    R.string.striga_validation_error_wrong_birthday_younger_than,
                    listOf(maximumYear)
                )
                false
            }
            else -> true
        }
    }

    private fun parseDateOrNull(value: String): LocalDate? {
        return kotlin.runCatching { LocalDate.parse(value, localDateFormatter) }
            .getOrNull()
    }
}
