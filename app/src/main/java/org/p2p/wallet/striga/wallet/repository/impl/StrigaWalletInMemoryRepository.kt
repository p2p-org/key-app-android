package org.p2p.wallet.striga.wallet.repository.impl

import org.p2p.wallet.striga.user.storage.StrigaStorageContract
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet

internal class StrigaWalletInMemoryRepository(
    private val strigaStorage: StrigaStorageContract
) {
    var fiatAccountDetails: StrigaFiatAccountDetails?
        get() = cachedFiatAccountDetails ?: strigaStorage.fiatAccount?.also { cachedFiatAccountDetails = it }
        set(value) {
            cachedFiatAccountDetails = value
            strigaStorage.fiatAccount = value
        }

    var cryptoAccountDetails: StrigaCryptoAccountDetails?
        get() = cachedCryptoAccountDetails ?: strigaStorage.cryptoAccount?.also { cachedCryptoAccountDetails = it }
        set(value) {
            cachedCryptoAccountDetails = value
            strigaStorage.cryptoAccount = value
        }

    var userWallet: StrigaUserWallet?
        get() = cachedUserWallet ?: strigaStorage.userWallet?.also { cachedUserWallet = it }
        set(value) {
            cachedUserWallet = value
            strigaStorage.userWallet = value
        }

    var userBankingDetails: StrigaUserBankingDetails?
        get() = cachedUserBankingDetails ?: strigaStorage.bankingDetails?.also { cachedUserBankingDetails = it }
        set(value) {
            cachedUserBankingDetails = value
            strigaStorage.bankingDetails = value
        }

    private var cachedFiatAccountDetails: StrigaFiatAccountDetails? = null
    private var cachedCryptoAccountDetails: StrigaCryptoAccountDetails? = null
    private var cachedUserWallet: StrigaUserWallet? = null
    private var cachedUserBankingDetails: StrigaUserBankingDetails? = null
}
