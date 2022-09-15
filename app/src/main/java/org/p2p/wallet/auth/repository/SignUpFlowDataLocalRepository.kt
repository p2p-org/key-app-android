package org.p2p.wallet.auth.repository

import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import org.p2p.solanaj.core.Account
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

private val TAG = SignUpFlowDataLocalRepository::class.simpleName.orEmpty()

/**
 * We're using this class to temporarily keep data that is collected
 * and generated during the whole Create wallet onboarding flow
 */
class SignUpFlowDataLocalRepository(
    private val signUpUserStorage: UserSignUpDetailsStorage,
    private val appScope: AppScope
) {

    private var smsResendTimers = listOf(30, 40, 60, 90, 120)

    var userPhoneNumberEnteredCount = 0
    var smsResendCount = 0

    var timerFlow: Flow<Int>? = null

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

    fun startTimer() {
        val timeInSeconds = smsResendTimers.getOrElse(smsResendCount) { smsResendTimers.first() }
        timerFlow = createSmsInputTimer(timeInSeconds)
        ++smsResendCount
    }

    fun resetSmsCount() {
        smsResendCount = 0
    }

    private fun createSmsInputTimer(
        timerSeconds: Int
    ): SharedFlow<Int> =
        (timerSeconds downTo 0)
            .asSequence()
            .asFlow()
            .onEach { delay(1.seconds.inWholeMilliseconds) }.shareIn(
                appScope,
                replay = 1,
                started = SharingStarted.WhileSubscribed()
            )

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
