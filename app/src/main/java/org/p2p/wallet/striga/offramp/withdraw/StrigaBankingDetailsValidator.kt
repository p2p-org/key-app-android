package org.p2p.wallet.striga.offramp.withdraw

import androidx.annotation.StringRes
import org.p2p.wallet.R

enum class StrigaWithdrawValidationResult(@StringRes val errorTextRes: Int?) {
    VALUE_IS_EMPTY(R.string.striga_withdrawal_validation_empty_error),
    INCORRECT_FORMAT(R.string.striga_withdrawal_validation_format_error),
    VALUE_IS_VALID(null)
}

class StrigaBankingDetailsValidator {
    fun validateIban(iban: String): StrigaWithdrawValidationResult {
        return when {
            iban.isBlank() -> StrigaWithdrawValidationResult.VALUE_IS_EMPTY
            else -> StrigaWithdrawValidationResult.VALUE_IS_VALID
        }
    }

    fun validateBic(bic: String): StrigaWithdrawValidationResult {
        return when {
            bic.isBlank() -> StrigaWithdrawValidationResult.VALUE_IS_EMPTY
            else -> StrigaWithdrawValidationResult.VALUE_IS_VALID
        }
    }
}
