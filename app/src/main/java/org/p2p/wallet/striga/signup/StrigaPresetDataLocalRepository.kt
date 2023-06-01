package org.p2p.wallet.striga.signup

import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds

interface StrigaPresetDataLocalRepository {
    fun getOccupationValuesList(): List<StrigaOccupation>
    fun getSourceOfFundsList(): List<StrigaSourceOfFunds>

    /**
     * @return true if the country is supported by Striga
     */
    fun checkIsCountrySupported(country: Country): Boolean
}
