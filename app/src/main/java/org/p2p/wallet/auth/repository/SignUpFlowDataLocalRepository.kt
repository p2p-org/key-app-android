package org.p2p.wallet.auth.repository

import com.google.gson.JsonObject
import org.p2p.solanaj.core.Account
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

private val TAG = SignUpFlowDataLocalRepository::class.simpleName.orEmpty()

/**
 * We're using this class to temporarily keep data that is collected
 * and generated during the whole Create wallet onboarding flow
 */
class SignUpFlowDataLocalRepository(
    private val signUpUserStorage: UserSignUpDetailsStorage
) {

   var userPhoneNumberEnteredCount = 0

    var signUpUserId: String? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("User Id is received and set: ${signUpUserId?.length}")
        }

    var userAccount: Account? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("Account is generated and set: ${userAccount?.publicKey}")
        }

    val userPublicKey: Base58String?
        get() = userAccount?.publicKey?.toBase58()?.toBase58Instance()
    val userPrivateKeyB58: Base58String?
        get() = userAccount?.secretKey?.toBase58Instance()

    val ethereumPublicKey: String?
        get() = signUpUserStorage.getLastSignUpUserDetails()?.signUpDetails?.ethereumPublicKey

    val thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta.ShareInnerDetails.ShareValue?
        get() = signUpUserStorage.getLastSignUpUserDetails()?.signUpDetails?.thirdShare

    val encryptedMnemonicPhrase: JsonObject?
        get() = signUpUserStorage.getLastSignUpUserDetails()?.signUpDetails?.encryptedMnemonicPhrase

    var userPhoneNumber: String? = null
        set(value) {
            field = value
            ++userPhoneNumberEnteredCount
            Timber.tag(TAG).i("User phone is received and set: ${userPhoneNumber?.length}")
        }

    fun generateUserAccount(userMnemonicPhrase: List<String>) {
        // BIP-44 by default
        userAccount = Account.fromBip44Mnemonic(
            words = userMnemonicPhrase,
            walletIndex = 0,
            passphrase = emptyString()
        )
    }

    fun clear() {
        this.signUpUserId = null
        this.userAccount = null
        this.userPhoneNumber = null
    }
}
