package org.p2p.wallet.auth.repository

import com.google.gson.JsonObject
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.utils.emptyString
import org.p2p.core.crypto.toBase58Instance
import timber.log.Timber

private const val TAG = "SignUpFlowDataLocalRepository"

/**
 * We're using this class to temporarily keep data that is collected
 * and generated during the whole Create wallet onboarding flow
 */
class SignUpFlowDataLocalRepository(
    private val signUpUserStorage: UserSignUpDetailsStorage
) {

    var userPhoneNumberEnteredCount = 0

    var isCreateWalletRequestSent = false

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
        get() = userAccount?.keypair?.toBase58Instance()

    val ethereumPublicKey: String?
        get() = signUpUserStorage.getLastSignUpUserDetails()?.signUpDetails?.ethereumPublicKey

    val thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta?
        get() = signUpUserStorage.getLastSignUpUserDetails()?.signUpDetails?.customThirdShare

    val encryptedMnemonicPhrase: JsonObject?
        get() = signUpUserStorage.getLastSignUpUserDetails()?.signUpDetails?.encryptedMnemonicPhrase

    var userPhoneNumber: PhoneNumber? = null
        set(value) {
            field = value
            ++userPhoneNumberEnteredCount
            Timber.tag(TAG).i("User phone is received and set: ${userPhoneNumber?.formattedValue?.length}")
        }

    var torusKey: String? = null
        set(value) {
            field = value
            Timber.tag(TAG).i("torus key is generated and set: ${torusKey?.length}")
        }

    fun generateUserAccount(userMnemonicPhrase: List<String>) {
        // BIP-44 by default
        userAccount = Account.fromBip44Mnemonic(
            words = userMnemonicPhrase,
            walletIndex = 0,
            passphrase = emptyString(),
            derivationPath = DerivationPath.BIP44CHANGE
        )
    }

    fun resetUserPhoneNumberEnteredCount() {
        userPhoneNumberEnteredCount = 0
    }

    fun clear() {
        this.signUpUserId = null
        this.userAccount = null
        this.userPhoneNumber = null
    }
}
