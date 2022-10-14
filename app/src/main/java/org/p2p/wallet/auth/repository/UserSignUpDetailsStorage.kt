package org.p2p.wallet.auth.repository

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key
import timber.log.Timber

private const val TAG = "UserSignUpDetailsStorage"

class UserSignUpDetailsStorage(
    private val accountStorage: AccountStorageContract,
) {

    data class SignUpUserDetails(
        @SerializedName("user_id")
        val userId: String,

        @SerializedName("details")
        val signUpDetails: Web3AuthSignUpResponse
    )

    fun save(data: Web3AuthSignUpResponse, userId: String, isCreate: Boolean = true): Boolean {
        val value = SignUpUserDetails(userId, data)

        accountStorage.saveObject(Key.KEY_LAST_DEVICE_SHARE_ID, value)
        if (isCreate) {
            accountStorage.saveString(Key.KEY_IN_SIGN_UP_PROCESS, "+")
        }

        Timber.tag(TAG).i("New user sign up details saved!")
        return true
    }

    fun getLastSignUpUserDetails(): SignUpUserDetails? {
        return kotlin.runCatching { accountStorage.getObject(Key.KEY_LAST_DEVICE_SHARE_ID, SignUpUserDetails::class) }
            .onSuccess { Timber.tag(TAG).i("Last sign up user details received!") }
            .onFailure { Timber.tag(TAG).i(it) }
            .getOrNull()
    }

    fun isDeviceShareSaved(): Boolean =
        getLastSignUpUserDetails()?.signUpDetails?.deviceShare != null && !isSignUpInProcess()

    fun isSignUpInProcess(): Boolean = accountStorage.getString(Key.KEY_IN_SIGN_UP_PROCESS) != null

    fun removeAllShares() {
        accountStorage.removeAll()
        removeLastDeviceShare()
    }

    private fun removeLastDeviceShare() {
        accountStorage.apply {
            remove(Key.KEY_LAST_DEVICE_SHARE_ID)
            remove(Key.KEY_IN_SIGN_UP_PROCESS)
        }
    }
}
