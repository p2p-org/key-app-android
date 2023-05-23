package org.p2p.wallet.striga.repository

import org.p2p.wallet.striga.repository.model.StrigaOccupation
import org.p2p.wallet.striga.repository.model.StrigaSourceOfFunds

interface StrigaKycDataLocalRepository {
    fun getOccupationValuesList(): List<StrigaOccupation>
    fun getSourceOfFundsList(): List<StrigaSourceOfFunds>
}
