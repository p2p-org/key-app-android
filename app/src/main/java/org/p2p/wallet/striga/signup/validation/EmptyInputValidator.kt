package org.p2p.wallet.striga.signup.validation

import org.p2p.core.common.TextContainer
import org.p2p.wallet.R

class EmptyInputValidator(
    override var errorMessage: TextContainer = TextContainer(R.string.striga_validation_error_empty)
) : InputValidator {

    override fun validate(input: String?): Boolean {
        return !input.isNullOrBlank()
    }
}
