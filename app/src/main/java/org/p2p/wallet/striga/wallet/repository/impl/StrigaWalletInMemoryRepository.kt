package org.p2p.wallet.striga.wallet.repository.impl

import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet

class StrigaWalletInMemoryRepository {
    var fiatAccountDetails: StrigaFiatAccountDetails? = null
    var userWallet: StrigaUserWallet? = null

    fun clear() {
        fiatAccountDetails = null
        userWallet = null
    }
}
