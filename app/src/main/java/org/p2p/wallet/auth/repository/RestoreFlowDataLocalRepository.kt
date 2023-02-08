package org.p2p.wallet.auth.repository

import com.google.gson.JsonObject
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

private const val TAG = "RestoreFlowDataLocalRepository"

class RestoreFlowDataLocalRepository(private val signUpDetailsStorage: UserSignUpDetailsStorage) {

    var isRestoreWalletRequestSent = false

    var userPhoneNumberEnteredCount = 0

    private var deviceShare: Web3AuthSignUpResponse.ShareDetailsWithMeta? = null

    val userRestorePublicKey: Base58String?
        get() = restoreUserKeyPair?.publicKey?.toBase58Instance()

    val userRestorePrivateKey: Base58String?
        get() = restoreUserKeyPair?.secretKey?.toBase58Instance()

    private var restoreUserKeyPair: TweetNaclFast.Signature.KeyPair? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("restoreUserKeyPair is generated and set: ${restoreUserKeyPair?.publicKey}")
        }

    var userPhoneNumber: PhoneNumber? = null
        set(value) {
            field = value
            ++userPhoneNumberEnteredCount
            Timber.tag(TAG).i("User phone is received and set: ${userPhoneNumber?.formattedValue?.length}")
        }

    suspend fun getDeviceShare(): Web3AuthSignUpResponse.ShareDetailsWithMeta? {
        return signUpDetailsStorage.getLastSignUpUserDetails()?.signUpDetails?.deviceShare
    }

    suspend fun saveDeviceShare(value: Web3AuthSignUpResponse.ShareDetailsWithMeta?) {
        deviceShare = value
        Timber.tag(TAG).i(
            "deviceShare is received and set: ${value?.innerShareDetails?.shareValue?.value?.length}"
        )
    }

    var customShare: Web3AuthSignUpResponse.ShareDetailsWithMeta? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("customShare is received and set: ${value?.innerShareDetails?.shareValue?.value?.length}")
        }

    var encryptedMnemonicJson: JsonObject? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("encryptedMnemonic is received and set: ${value != null}")
        }

    var userActualAccount: Account? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("userActualAccount is generated and set")
        }

    var torusKey: String? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("torusKey is generated and set: ${torusKey?.length}")
        }

    var socialShareUserId: String? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("socialShareUserId is generated and set: ${socialShareUserId?.length}")
        }

    fun generateRestoreUserKeyPair() {
        restoreUserKeyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(TweetNaclFast.Signature.keyPair().secretKey)
    }

    fun generateActualAccount(userSeedPhrase: List<String>) {
        this.userActualAccount = Account.fromBip44Mnemonic(
            words = userSeedPhrase,
            walletIndex = 0,
            passphrase = emptyString(),
            derivationPath = DerivationPath.BIP44CHANGE
        )
    }

    fun resetUserPhoneNumberEnteredCount() {
        userPhoneNumberEnteredCount = 0
    }
}
