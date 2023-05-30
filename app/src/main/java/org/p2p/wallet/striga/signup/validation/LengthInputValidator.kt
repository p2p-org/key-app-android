package org.p2p.wallet.striga.signup.validation

import org.p2p.core.common.TextContainer
import org.p2p.wallet.R

class LengthInputValidator(
    private val minLength: Int = 1
): InputValidator {

    override var errorMessage: TextContainer = TextContainer(R.string.striga_validation_error_too_short)

    override fun validate(input: String?): Boolean {
        return (input?.length ?: 0) > minLength
    }
}
