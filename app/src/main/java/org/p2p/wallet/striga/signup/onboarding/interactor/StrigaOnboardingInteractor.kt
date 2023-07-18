package org.p2p.wallet.striga.signup.onboarding.interactor

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaOccupation
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.presetpicker.repository.StrigaPresetDataLocalRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaOnboardingInteractor(
    private val countryRepository: CountryCodeRepository,
    private val strigaPresetDataLocalRepository: StrigaPresetDataLocalRepository,
    private val signupDataRepository: StrigaSignupDataLocalRepository
) {
    suspend fun getChosenCountry(): CountryCode {
        return signupDataRepository.getUserSignupDataByType(StrigaSignupDataType.COUNTRY_ALPHA_2)
            .successOrNull()
            ?.value
            ?.let { countryRepository.findCountryCodeByIsoAlpha2(it) }
            ?: countryRepository.detectCountryOrDefault()
    }

    suspend fun saveCurrentCountry(country: CountryCode) {
        signupDataRepository.updateSignupData(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_ALPHA_2, country.nameCodeAlpha2)
        )
    }

    fun checkIsCountrySupported(country: CountryCode): Boolean {
        return strigaPresetDataLocalRepository.checkIsCountrySupported(country)
    }

    fun getOccupationByName(name: String): StrigaOccupation? {
        return strigaPresetDataLocalRepository.getOccupationValuesList().firstOrNull { it.occupationName == name }
    }

    fun getSourcesOfFundsByName(name: String): StrigaSourceOfFunds? {
        return strigaPresetDataLocalRepository.getSourceOfFundsList().firstOrNull { it.sourceName == name }
    }
}
