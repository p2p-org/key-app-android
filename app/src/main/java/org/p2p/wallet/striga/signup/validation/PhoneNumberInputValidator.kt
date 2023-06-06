package org.p2p.wallet.striga.signup.validation

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.CountryCodeLocalRepository

class PhoneNumberInputValidator(
    val phoneCode: String,
    val phoneNumber: String
) : InputValidator, KoinComponent {

    override var errorMessage: TextContainer = TextContainer(R.string.striga_validation_error_phone_number)
    private val countryCodeRepository: CountryCodeLocalRepository by inject()

    override fun validate(input: String?): Boolean {
        return countryCodeRepository.isValidNumberForRegion(phoneNumber, phoneCode)
    }
}
