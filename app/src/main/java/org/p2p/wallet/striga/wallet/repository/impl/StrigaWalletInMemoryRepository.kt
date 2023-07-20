package org.p2p.wallet.striga.wallet.repository.impl

import org.p2p.wallet.striga.user.storage.StrigaStorageContract
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet

internal class StrigaWalletInMemoryRepository(
    private val strigaStorage: StrigaStorageContract
) {
    var fiatAccountDetails: StrigaFiatAccountDetails?
        get() = strigaStorage.fiatAccount
        set(value) {
            strigaStorage.fiatAccount = value
        }

    var cryptoAccountDetails: StrigaCryptoAccountDetails?
        get() = strigaStorage.cryptoAccount
        set(value) {
            strigaStorage.cryptoAccount = value
        }

    var userWallet: StrigaUserWallet?
        get() = strigaStorage.userWallet
        set(value) {
            strigaStorage.userWallet = value
        }
}
