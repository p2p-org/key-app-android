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
    @SerializedName("device_name")
    val deviceShareDeviceName: String,
    @SerializedName("phone_number")
    val customSharePhoneNumberE164: String,
    @SerializedName("email")
    val socialShareOwnerEmail: String,
) {
    @SerializedName("auth_provider")
    val authProvider: String = "Google" // always google for Android
}
