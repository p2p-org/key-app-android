package org.p2p.wallet.striga.presetpicker.interactor

import timber.log.Timber
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataToPick
import org.p2p.wallet.striga.signup.StrigaPresetDataLocalRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaPresetDataInteractor(
    private val presetDataRepository: StrigaPresetDataLocalRepository,
    private val countryRepository: CountryRepository,
    private val strigaSignupDataRepository: StrigaSignupDataLocalRepository
) {

    suspend fun saveSelectedPresetData(
        type: StrigaPresetDataToPick,
        item: StrigaPresetDataItem
    ): StrigaDataLayerResult<Unit> {
        val dataToSave: String? = when (item) {
            is StrigaPresetDataItem.StrigaCountryItem -> when (type) {
                StrigaPresetDataToPick.CURRENT_ADDRESS_COUNTRY -> item.details.codeAlpha2
                StrigaPresetDataToPick.COUNTRY_OF_BIRTH -> item.details.codeAlpha3
                else -> Timber.i("Illegal $item for type $type to save in db").run { null }
            }
            is StrigaPresetDataItem.StrigaOccupationItem -> item.details.occupationName
            is StrigaPresetDataItem.StrigaSourceOfFundsItem -> item.details.sourceName
        }

        return dataToSave
            ?.let { StrigaSignupData(type.toSignupDataType(), it) }
            ?.let { strigaSignupDataRepository.updateSignupData(it) }
            ?: StrigaDataLayerResult.Success(Unit)
    }

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
        StrigaPresetDataToPick.CURRENT_ADDRESS_COUNTRY -> StrigaSignupDataType.COUNTRY
        StrigaPresetDataToPick.COUNTRY_OF_BIRTH -> StrigaSignupDataType.COUNTRY_OF_BIRTH
        StrigaPresetDataToPick.SOURCE_OF_FUNDS -> StrigaSignupDataType.SOURCE_OF_FUNDS
        StrigaPresetDataToPick.OCCUPATION -> StrigaSignupDataType.OCCUPATION
    }
}
