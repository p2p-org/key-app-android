package org.p2p.wallet.auth.gateway.repository.model

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.utils.emptyString

/**
 * Our own structure with custom fields that is sent while creating wallet.
 * It keeps some custom data that we can get when user starts to restore wallet.
 * Working scheme:
 * 1. create and fill this struct with custom data
 * 2. send it on confirm_create_wallet
 * 3. backend keeps this structure and doesn't modify it
 * 4. receive exact same struct when confirm_restore_wallet
 *
 * !!
 * The set of fields is custom and is not declared by the backend
 * Fields can be changed after some discussions inside the Android and iOS team
 * Serialized names should be exact as names on iOS side
 * !!
 */
data class GatewayOnboardingMetadata(
    @SerializedName("eth_public")
    val ethPublic: String = emptyString(),
    @SerializedName("meta_timestamp")
    val metaTimestamp: Long,
    @SerializedName("device_name")
    val deviceShareDeviceName: String,
    @SerializedName("phone_number")
    val customSharePhoneNumberE164: String,
    @SerializedName("phone_number_timestamp")
    val phoneNumberTimestamp: Long = 0,
    @SerializedName("email")
    val socialShareOwnerEmail: String,
    @SerializedName("email_timestamp")
    val emailTimestamp: Long = 0,
    @SerializedName("auth_provider_timestamp")
    val authProviderTimestamp: Long = 0,
    @SerializedName("striga")
    val strigaMetadata: StrigaMetadata? = null,
) {
    @SerializedName("auth_provider")
    val authProvider: String = "Google" // always google for Android

    data class StrigaMetadata(
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("user_id_timestamp")
        val userIdTimestamp: Long
    )
}
