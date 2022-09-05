package org.p2p.wallet.auth.repository

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import timber.log.Timber

private const val TAG = "UserSignUpDetailsStorage"
private const val KEY_LAST_DEVICE_SHARE_ID = "KEY_LAST_DEVICE_SHARE_ID"

class UserSignUpDetailsStorage(
    private val secureStorage: SecureStorageContract,
) {

    private fun generatePrefsKey(userId: String): String = "${userId}_sign_up_details"

    data class SignUpUserDetails(
        @SerializedName("user_id")
        val userId: String,

        @SerializedName("details")
        val signUpDetails: Web3AuthSignUpResponse
    )

    fun save(data: Web3AuthSignUpResponse, userId: String): Boolean {
        val key = generatePrefsKey(userId)
        val value = SignUpUserDetails(userId, data)

        secureStorage.saveObject(key, value)
        secureStorage.saveObject(KEY_LAST_DEVICE_SHARE_ID, value)

        Timber.tag(TAG).i("New user sign up details saved!")
        return true
    }

    fun isUserSignUpDetailsSaved(userId: String): Boolean {
        return secureStorage.contains(generatePrefsKey(userId))
    }

    fun getUserSignUpDetailsById(userId: String): SignUpUserDetails? {
        return kotlin.runCatching { secureStorage.getObject(generatePrefsKey(userId), SignUpUserDetails::class) }
            .onFailure { Timber.i(it) }
            .getOrNull()
    }

    fun getLastSignUpUserDetails(): SignUpUserDetails? {
        return kotlin.runCatching { secureStorage.getObject(KEY_LAST_DEVICE_SHARE_ID, SignUpUserDetails::class) }
            .onSuccess { Timber.tag(TAG).i("Last sign up user details received!") }
            .onFailure { Timber.tag(TAG).i(it) }
            .getOrNull()
    }

    fun isDeviceShareSaved(): Boolean = getLastSignUpUserDetails() != null
}
