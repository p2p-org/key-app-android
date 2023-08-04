package org.p2p.core.utils.validators

class BankingBicValidator {
    companion object {
        private val BIC_REGEX = Regex("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}[0-9]{0,3}\$")

        private const val BIC_MIN_SIZE = 8
        private const val BIC_MAX_SIZE = 11
    }

    fun validateBic(bic: String): BicValidationResult {
        return when {
            bic.isBlank() -> BicValidationResult.Error.BicIsEmpty
            bic.length < BIC_MIN_SIZE -> BicValidationResult.Error.BicIsTooShort
            bic.length > BIC_MAX_SIZE -> BicValidationResult.Error.BicIsTooLong
            BIC_REGEX.matches(bic) -> BicValidationResult.Error.BicIsNotValid
            else -> BicValidationResult.BicIsValid
        }
    }
}
