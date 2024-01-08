package org.p2p.core.utils.validators

import org.p2p.core.utils.emptyString

class BankingIbanValidator {
    private companion object {
        // iban length depends on the country but we take max value
        private const val MAX_IBAN_SIZE = 34
        private const val MIN_IBAN_SIZE = 5
    }

    /**
     * An IBAN is validated by converting it into an integer and performing a basic mod-97 operation
     * (as described in ISO 7064) on it.
     * If the IBAN is valid, the remainder equals 1
     */
    fun validateIban(iban: String): IbanValidationResult {
        return when {
            iban.isBlank() -> IbanValidationResult.Error.IbanIsEmpty
            iban.length < MIN_IBAN_SIZE -> IbanValidationResult.Error.IbanIsTooShort
            iban.length > MAX_IBAN_SIZE -> IbanValidationResult.Error.IbanIsTooLong
            !isIbanValidByValue(iban) -> IbanValidationResult.Error.IbanIsNotValid
            else -> IbanValidationResult.IbanIsValid
        }
    }

    private fun isIbanValidByValue(iban: String): Boolean {
        // check for country code
        val isFirstTwoAreLetters = iban[0].isLetter() && iban[1].isLetter()
        // check for check digits
        val isSecondTwoAreDigits = iban[2].isDigit() && iban[3].isDigit()
        if (!isFirstTwoAreLetters || !isSecondTwoAreDigits) {
            return false
        }

        // move first 4 chars to the end like GB341111 -> 1111GB34
        val rearrangedIban = iban.substring(4) + iban.substring(0, 4)
        if (rearrangedIban.any { !it.isLetterOrDigit() }) {
            return false
        }

        val numericIban = convertToNumeric(rearrangedIban)
            .ifBlank { return false }
            .toBigInteger()

        // Interpret the result string as a decimal integer
        // and compute the remainder of that number on division by 97
        // If the remainder is 1,
        // the check digit test is passed and the IBAN might be valid.
        return numericIban.mod(97.toBigInteger()).toInt() == 1
    }

    /**
     * Replace each letter in the string with two digits, thereby expanding the string,
     * starting with A = 10, B = 11, ..., Z = 35
     *
     * Example (fictitious United Kingdom bank, sort code 12-34-56, account number 98765432):
     * original IBAN: GB82WEST12345698765432
     * Rearranged: WEST12345698765432GB82 <---
     * Convert to numeric: 3214282912345698765432161182
     */
    private fun convertToNumeric(rearrangedIban: String): String = buildString {
        rearrangedIban.forEach { char ->
            val numericChar = when {
                char.isDigit() -> char.toString()
                char.isLetter() -> (char.uppercaseChar().code - 'A'.code + 10).toString()
                else -> return emptyString()
            }
            append(numericChar)
        }
    }
}
