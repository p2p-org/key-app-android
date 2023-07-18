package org.p2p.wallet.striga.signup.steps.validation

import org.p2p.core.common.TextContainer
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.CountryCodeRepository

/**
 * @param regionCodeAlpha2 ISO alpha2 format: EN, RU or KZ
 */
class PhoneNumberInputValidator(
    val regionCodeAlpha2: String,
    private val countryCodeRepository: CountryCodeRepository,
) : InputValidator {

    override var errorMessage: TextContainer = TextContainer(R.string.striga_validation_error_phone_number)

    override fun validate(input: String?): Boolean {
        if (input.isNullOrBlank()) return false
        val phoneNumber = input.replace("\\D".toRegex(), "")
        return countryCodeRepository.isValidNumberForRegion(phoneNumber, regionCodeAlpha2)
    }
}
