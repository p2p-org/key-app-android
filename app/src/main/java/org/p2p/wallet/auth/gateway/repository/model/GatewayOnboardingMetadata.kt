package org.p2p.wallet.auth.gateway.repository.model

import com.google.gson.annotations.SerializedName

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
    // v1
    @SerializedName("device_name")
    val deviceShareDeviceName: String,
    @SerializedName("phone_number")
    val customSharePhoneNumberE164: String,
    @SerializedName("email")
    val socialShareOwnerEmail: String,
    // v2
    @SerializedName("eth_public")
    val ethPublic: String? = null,
    @SerializedName("meta_timestamp")
    val metaTimestampSec: Long,
    @SerializedName("device_name_timestamp")
    val deviceNameTimestampSec: Long = 0,
    @SerializedName("phone_number_timestamp")
    val phoneNumberTimestampSec: Long = 0,
    @SerializedName("email_timestamp")
    val emailTimestampSec: Long = 0,
    @SerializedName("auth_provider_timestamp")
    val authProviderTimestampSec: Long = 0,
    @SerializedName("striga")
    val strigaMetadata: StrigaMetadata? = null,
) {
    @SerializedName("auth_provider")
    val authProvider: String = "Google" // always google for Android

    data class StrigaMetadata(
        @SerializedName("user_id")
        val userId: String?,
        @SerializedName("user_id_timestamp")
        val userIdTimestamp: Long
    )
}
