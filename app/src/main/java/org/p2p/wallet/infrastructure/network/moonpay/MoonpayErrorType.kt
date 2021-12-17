package org.p2p.wallet.infrastructure.network.moonpay

import com.google.gson.annotations.SerializedName

enum class MoonpayErrorType {
    @SerializedName("BadRequestError")
    BadRequestError,

    @SerializedName("NotFoundError")
    NotFoundError,

    @SerializedName("UnauthorizedError")
    UnauthorizedError
}