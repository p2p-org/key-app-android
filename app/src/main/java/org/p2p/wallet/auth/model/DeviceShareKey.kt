package org.p2p.wallet.auth.model

import com.google.gson.annotations.SerializedName

data class DeviceShareKey(
    @SerializedName("reconstructedETH") val reconstructedKey: String,
    @SerializedName("privateSOL") val privateKey: String,
    @SerializedName("deviceShare") val share: DeviceShare?,
    @SerializedName("userId") var userId: String?,
) {
    data class DeviceShare(
        @SerializedName("share") val share: DeviceLocalShare,
        @SerializedName("p2p") val verifier: Verifier,
    ) {
        data class DeviceLocalShare(
            @SerializedName("share") val share: DeviceShareData,
            @SerializedName("polynomialID") val polynomialID: String,
        ) {
            data class DeviceShareData(
                @SerializedName("share") val share: String,
                @SerializedName("shareIndex") val shareIndex: String,
            )
        }

        data class Verifier(
            @SerializedName("verifier") val verifier: String,
            @SerializedName("storage") val storage: String,
            @SerializedName("type") val type: String,
        )
    }
}
