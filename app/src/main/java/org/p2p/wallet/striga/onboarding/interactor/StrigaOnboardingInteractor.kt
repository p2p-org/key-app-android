package org.p2p.wallet.striga.onboarding.interactor

import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.StrigaPresetDataLocalRepository

class StrigaOnboardingInteractor(
    private val countryRepository: CountryRepository,
    private val strigaPresetDataLocalRepository: StrigaPresetDataLocalRepository
) {
    suspend fun getDefaultCountry(): Country {
        // todo: get saved country and return it if exists
        return countryRepository.detectCountryOrDefault()
    }

    fun checkIsCountrySupported(country: Country): Boolean {
        return strigaPresetDataLocalRepository.checkIsCountrySupported(country)
    }

    suspend fun getAllCountries(): List<Country> = countryRepository.getAllCountries()

    fun getOccupationValuesList(): List<StrigaOccupation> = strigaPresetDataLocalRepository.getOccupationValuesList()

    fun getSourceOfFundsList(): List<StrigaSourceOfFunds> = strigaPresetDataLocalRepository.getSourceOfFundsList()

    fun getOccupationByName(name: String): StrigaOccupation {
        return strigaPresetDataLocalRepository.getOccupationValuesList().first { it.occupationName == name }
    }

    fun getSourcesOfFundsByName(name: String): StrigaSourceOfFunds {
        return strigaPresetDataLocalRepository.getSourceOfFundsList().first { it.sourceName == name }
    }
}
