package org.p2p.wallet.striga.user.api.request

import com.google.gson.annotations.SerializedName

/**
 * @param status - [org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus] as string value
 */
class StrigaSimulateUserStatusRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("status")
    val status: String
)
