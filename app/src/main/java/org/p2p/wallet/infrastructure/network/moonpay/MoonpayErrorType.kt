package org.p2p.wallet.infrastructure.network.moonpay

import com.google.gson.annotations.SerializedName

enum class MoonpayErrorType(val stringValue: String) {
    @SerializedName("BadRequestError")
    BAD_REQUEST_ERROR("BadRequestError"),

    @SerializedName("NotFoundError")
    NOT_FOUND_ERROR("NotFoundError"),

    @SerializedName("ParamNormalizationError")
    PARAM_NORMALIZATION_ERROR("ParamNormalizationError"),

    @SerializedName("UnauthorizedError")
    UNAUTHORIZED_ERROR("UnauthorizedError"),

    @SerializedName("UnknownError")
    UNKNOWN_ERROR("UnknownError");

    companion object {
        fun parse(type: String): MoonpayErrorType = when (type) {
            BAD_REQUEST_ERROR.name -> BAD_REQUEST_ERROR
            NOT_FOUND_ERROR.name -> NOT_FOUND_ERROR
            UNAUTHORIZED_ERROR.name -> UNAUTHORIZED_ERROR
            else -> BAD_REQUEST_ERROR
        }
    }
}
