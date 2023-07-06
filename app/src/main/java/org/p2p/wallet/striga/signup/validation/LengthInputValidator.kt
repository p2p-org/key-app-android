package org.p2p.wallet.striga.signup.validation

import org.p2p.core.common.TextContainer
import org.p2p.core.utils.orZero
import org.p2p.wallet.R

class LengthInputValidator(
    private val minLength: Int = 2,
    private val maxLength: Int = -1,
    private val checkTrimmedValue: Boolean = true
) : InputValidator {

    /**
     * @param range - inclusive range of valid lengths
     */
    constructor(range: IntRange, checkTrimmedValue: Boolean = true) : this(range.first, range.last, checkTrimmedValue)

    override var errorMessage: TextContainer = TextContainer(R.string.striga_validation_error_too_short)

    override fun validate(input: String?): Boolean {
        val actualLength = if (checkTrimmedValue) {
            input?.trim()?.length.orZero()
        } else {
            input?.length.orZero()
        }
        return when {
            actualLength < minLength -> {
                errorMessage = TextContainer(R.string.striga_validation_error_too_short)
                false
            }
            maxLength != -1 && actualLength > maxLength -> {
                errorMessage = TextContainer(R.string.striga_validation_error_too_long)
                false
            }
            else -> true
        }
    }
}
