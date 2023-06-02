package org.p2p.wallet.striga.onboarding.interactor

import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.striga.signup.StrigaPresetDataLocalRepository
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaOnboardingInteractor(
    private val countryRepository: CountryRepository,
    private val strigaPresetDataLocalRepository: StrigaPresetDataLocalRepository,
    private val signupDataRepository: StrigaSignupDataLocalRepository
) {
    suspend fun getChosenCountry(): Country {
        return signupDataRepository.getUserSignupDataByType(StrigaSignupDataType.COUNTRY)
            .successOrNull()
            ?.value
            ?.let { countryRepository.findCountryByIsoAlpha2(it) }
            ?: countryRepository.detectCountryOrDefault()
    }

    fun checkIsCountrySupported(country: Country): Boolean {
        return strigaPresetDataLocalRepository.checkIsCountrySupported(country)
    }

    fun getOccupationByName(name: String): StrigaOccupation? {
        return strigaPresetDataLocalRepository.getOccupationValuesList().firstOrNull { it.occupationName == name }
    }

    fun getSourcesOfFundsByName(name: String): StrigaSourceOfFunds? {
        return strigaPresetDataLocalRepository.getSourceOfFundsList().firstOrNull { it.sourceName == name }
    }
}
