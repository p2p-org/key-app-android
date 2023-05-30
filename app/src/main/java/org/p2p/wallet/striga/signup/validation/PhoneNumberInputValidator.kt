package org.p2p.wallet.striga.signup.validation

import java.util.regex.Pattern
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R

private val PATTERN = Pattern.compile(
    "^\\+[0-9]{10,15}\$"
)

class PhoneNumberInputValidator: InputValidator {

    override var errorMessage: TextContainer = TextContainer(R.string.striga_validation_error_phone_number)

    override fun validate(input: String?): Boolean {
        return input?.replace(" ", "")?.matches(PATTERN.toRegex()) ?: false
    }
}
