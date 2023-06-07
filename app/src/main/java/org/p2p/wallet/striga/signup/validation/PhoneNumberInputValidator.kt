package org.p2p.wallet.striga.signup.validation

import org.p2p.core.common.TextContainer
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.CountryCodeRepository

class PhoneNumberInputValidator(
    val regionCode: String,
    val phoneNumber: String,
    private val countryCodeRepository: CountryCodeRepository,
) : InputValidator {

    override var errorMessage: TextContainer = TextContainer(R.string.striga_validation_error_phone_number)

    /**
     * @param [regionCode] EN, RU or KZ
     * @param[phoneNumber] trimmed value without regionCode
     */
    override fun validate(input: String?): Boolean {
        return countryCodeRepository.isValidNumberForRegion(phoneNumber, regionCode)
    }
}
