package org.p2p.wallet.striga.offramp.withdraw

import androidx.annotation.StringRes
import org.p2p.core.utils.validators.BankingBicValidator
import org.p2p.core.utils.validators.BankingIbanValidator
import org.p2p.core.utils.validators.BicValidationResult
import org.p2p.core.utils.validators.IbanValidationResult
import org.p2p.wallet.R

enum class StrigaWithdrawValidationResult(@StringRes val errorTextRes: Int?) {
    VALUE_IS_EMPTY(R.string.striga_withdraw_validation_empty_error),
    INCORRECT_FORMAT(R.string.striga_withdraw_validation_format_error),
    VALUE_IS_VALID(null)
}

class StrigaBankingDetailsValidator(
    private val ibanValidator: BankingIbanValidator,
    private val bicValidator: BankingBicValidator
) {

    fun validateIban(iban: String): StrigaWithdrawValidationResult {
        return when (ibanValidator.validateIban(iban)) {
            IbanValidationResult.Error.IbanIsEmpty -> StrigaWithdrawValidationResult.VALUE_IS_EMPTY
            is IbanValidationResult.Error -> StrigaWithdrawValidationResult.INCORRECT_FORMAT
            else -> StrigaWithdrawValidationResult.VALUE_IS_VALID
        }
    }

    fun validateBic(bic: String): StrigaWithdrawValidationResult {
        return when (bicValidator.validateBic(bic)) {
            BicValidationResult.Error.BicIsEmpty -> StrigaWithdrawValidationResult.VALUE_IS_EMPTY
            is BicValidationResult.Error -> StrigaWithdrawValidationResult.INCORRECT_FORMAT
            else -> StrigaWithdrawValidationResult.VALUE_IS_VALID
        }
    }
}
