package org.p2p.core.utils.validators

sealed interface IbanValidationResult {
    object IbanIsValid : IbanValidationResult

    sealed interface Error : IbanValidationResult {
        object IbanIsEmpty : Error
        object IbanIsTooShort : Error
        object IbanIsTooLong : Error
        object IbanIsNotValid : Error
    }
}
