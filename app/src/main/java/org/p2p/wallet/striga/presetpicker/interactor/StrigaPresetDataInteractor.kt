package org.p2p.wallet.striga.presetpicker.interactor

import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataToPick
import org.p2p.wallet.striga.signup.StrigaPresetDataLocalRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaPresetDataInteractor(
    private val presetDataRepository: StrigaPresetDataLocalRepository,
    private val countryRepository: CountryRepository,
    private val strigaSignupDataRepository: StrigaSignupDataLocalRepository
) {

    suspend fun getPresetData(type: StrigaPresetDataToPick): List<StrigaPresetDataItem> {
        return when (type) {
            StrigaPresetDataToPick.CURRENT_ADDRESS_COUNTRY, StrigaPresetDataToPick.COUNTRY_OF_BIRTH -> {
                countryRepository.getAllCountries().map(StrigaPresetDataItem::StrigaCountryItem)
            }
            StrigaPresetDataToPick.SOURCE_OF_FUNDS -> {
                presetDataRepository.getSourceOfFundsList().map(StrigaPresetDataItem::StrigaSourceOfFundsItem)
            }
            StrigaPresetDataToPick.OCCUPATION -> {
                presetDataRepository.getOccupationValuesList().map(StrigaPresetDataItem::StrigaOccupationItem)
            }
        }
    }

    suspend fun getSelectedPresetDataItem(type: StrigaPresetDataToPick): StrigaPresetDataItem? {
        val selectedValue = strigaSignupDataRepository.getUserSignupDataByType(type.toSignupDataType())
            .successOrNull()
            ?.value
            ?: return null

        return createPresetDataItem(type, selectedValue)
    }

    private suspend fun createPresetDataItem(type: StrigaPresetDataToPick, value: String): StrigaPresetDataItem? {
        return when (type) {
            StrigaPresetDataToPick.CURRENT_ADDRESS_COUNTRY -> {
                countryRepository.findCountryByIsoAlpha2(value)?.let {
                    StrigaPresetDataItem.StrigaCountryItem(it)
                }
            }
            StrigaPresetDataToPick.COUNTRY_OF_BIRTH -> {
                countryRepository.findCountryByIsoAlpha3(value)?.let {
                    StrigaPresetDataItem.StrigaCountryItem(it)
                }
            }
            StrigaPresetDataToPick.SOURCE_OF_FUNDS -> {
                presetDataRepository.getSourceOfFundsList()
                    .firstOrNull { it.sourceName == value }
                    ?.let { StrigaPresetDataItem.StrigaSourceOfFundsItem(it) }
            }
            StrigaPresetDataToPick.OCCUPATION -> {
                presetDataRepository.getOccupationValuesList()
                    .firstOrNull { it.occupationName == value }
                    ?.let { StrigaPresetDataItem.StrigaOccupationItem(it) }
            }
        }
    }

    private fun StrigaPresetDataToPick.toSignupDataType(): StrigaSignupDataType = when (this) {
        StrigaPresetDataToPick.CURRENT_ADDRESS_COUNTRY -> StrigaSignupDataType.COUNTRY_ALPHA_2
        StrigaPresetDataToPick.COUNTRY_OF_BIRTH -> StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3
        StrigaPresetDataToPick.SOURCE_OF_FUNDS -> StrigaSignupDataType.SOURCE_OF_FUNDS
        StrigaPresetDataToPick.OCCUPATION -> StrigaSignupDataType.OCCUPATION
    }
}
