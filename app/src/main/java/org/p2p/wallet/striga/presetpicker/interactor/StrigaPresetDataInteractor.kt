package org.p2p.wallet.striga.presetpicker.interactor

import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.striga.signup.StrigaPresetDataLocalRepository

class StrigaPresetDataInteractor(
    private val presetDataRepository: StrigaPresetDataLocalRepository,
    private val countryRepository: CountryCodeRepository,
) {

    suspend fun getPresetData(type: StrigaPresetDataItem): List<StrigaPresetDataItem> {
        return when (type) {
            is StrigaPresetDataItem.Country -> {
                countryRepository.getCountryCodes().map(StrigaPresetDataItem::Country)
            }
            is StrigaPresetDataItem.SourceOfFunds -> {
                presetDataRepository.getSourceOfFundsList().map(StrigaPresetDataItem::SourceOfFunds)
            }
            is StrigaPresetDataItem.Occupation -> {
                presetDataRepository.getOccupationValuesList().map(StrigaPresetDataItem::Occupation)
            }
        }
    }
}
