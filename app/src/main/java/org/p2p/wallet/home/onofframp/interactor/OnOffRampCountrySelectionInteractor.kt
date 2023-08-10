package org.p2p.wallet.home.onofframp.interactor

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class OnOffRampCountrySelectionInteractor(
    private val countryRepository: CountryCodeRepository,
    private val strigaSignupDataRepository: StrigaSignupDataLocalRepository,
    private val settingsInteractor: SettingsInteractor
) {
    suspend fun getChosenCountry(): CountryCode {
        return settingsInteractor.userCountryCode ?: countryRepository.detectCountryOrDefault()
    }

    suspend fun saveCurrentCountry(country: CountryCode) {
        settingsInteractor.userCountryCode = country

        strigaSignupDataRepository.updateSignupData(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_ALPHA_2, country.nameCodeAlpha2)
        )
    }
}
