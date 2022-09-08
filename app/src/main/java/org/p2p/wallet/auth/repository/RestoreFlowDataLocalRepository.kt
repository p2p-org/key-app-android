package org.p2p.wallet.auth.repository

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

private val TAG = RestoreFlowDataLocalRepository::class.simpleName.orEmpty()

class RestoreFlowDataLocalRepository {

    val userRestorePublicKey: Base58String?
        get() = restoreUserKeyPair?.publicKey?.toBase58Instance()

    val userRestorePrivateKey: Base58String?
        get() = restoreUserKeyPair?.secretKey?.toBase58Instance()

    private var restoreUserKeyPair: TweetNaclFast.Signature.KeyPair? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("Account is generated and set: ${restoreUserKeyPair?.publicKey}")
        }

    var userPhoneNumber: PhoneNumber? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("User phone is received and set: ${userPhoneNumber?.value?.length}")
        }

    var customShare: Web3AuthSignUpResponse.ShareDetailsWithMeta? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("thirdShare is received and set")
        }

    var encryptedMnemonic: String? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("encryptedMnemonic is received and set")
        }

    var userActualAccount: Account? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("userActualAccount is generated and set")
        }

    var socialShare: String? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("socialShare is generated and set: ${socialShare?.length}")
        }

    var socialShareUserId: String? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("socialShareUserId is generated and set: ${socialShareUserId?.length}")
        }

    fun generateRestoreUserKeyPair() {
        Timber.tag("______").d("Restoring keys")
        restoreUserKeyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(TweetNaclFast.Signature.keyPair().secretKey)
        Timber.tag("______").d("${restoreUserKeyPair?.publicKey}")
        Timber.tag("______").d("${restoreUserKeyPair?.secretKey}")
    }

    fun generateActualAccount(userSeedPhrase: List<String>) {
        this.userActualAccount = Account.fromBip44Mnemonic(userSeedPhrase, walletIndex = 0, passphrase = emptyString())
    }
}
