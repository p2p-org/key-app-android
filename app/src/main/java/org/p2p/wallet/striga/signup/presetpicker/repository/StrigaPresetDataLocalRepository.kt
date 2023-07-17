package org.p2p.wallet.striga.signup.presetpicker.repository

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaOccupation
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaSourceOfFunds

interface StrigaPresetDataLocalRepository {
    fun getOccupationValuesList(): List<StrigaOccupation>
    fun getSourceOfFundsList(): List<StrigaSourceOfFunds>

    /**
     * @return true if the country is supported by Striga
     */
    fun checkIsCountrySupported(country: CountryCode): Boolean
}
