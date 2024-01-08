package org.p2p.core.utils.validators

/**
 * The BIC is an 8 character code, defined as ‘business party identifier’,
 * consisting of the business party prefix (4 alphanumeric),
 * the country code as defined in ISO 3166-1 (2 alphabetic),
 * and the business party suffix (2 alphanumeric).
 * example: AAAABB11222 OR AAAA-BB-11-222
 */
sealed interface BicValidationResult {
    object BicIsValid : BicValidationResult

    sealed interface Error : BicValidationResult {
        object BicIsEmpty : Error
        object BicIsTooShort : Error
        object BicIsTooLong : Error
        object BicIsNotValid : Error
    }
}
