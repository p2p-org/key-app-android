package org.p2p.wallet.striga.wallet.repository

import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet

class StrigaWalletInMemoryRepository {
    var fiatAccountDetails: StrigaFiatAccountDetails? = null
    var userStrigaWallet: StrigaUserWallet? = null

    fun clear() {
        fiatAccountDetails = null
        userStrigaWallet = null
    }
}
