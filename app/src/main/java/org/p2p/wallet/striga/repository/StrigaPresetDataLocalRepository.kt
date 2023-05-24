package org.p2p.wallet.striga.repository

import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.striga.repository.model.StrigaOccupation
import org.p2p.wallet.striga.repository.model.StrigaSourceOfFunds

interface StrigaPresetDataLocalRepository {
    fun getOccupationValuesList(): List<StrigaOccupation>
    fun getSourceOfFundsList(): List<StrigaSourceOfFunds>

    /**
     * @return true if the country is supported by Striga
     */
    fun checkIsSupportedCountry(country: Country): Boolean
}
