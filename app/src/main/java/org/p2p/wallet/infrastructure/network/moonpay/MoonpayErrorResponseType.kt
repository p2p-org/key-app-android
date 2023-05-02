package org.p2p.wallet.infrastructure.network.moonpay

import com.google.gson.annotations.SerializedName

enum class MoonpayErrorResponseType(val stringValue: String) {
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
        fun fromStringValue(type: String?): MoonpayErrorResponseType {
            return values().find { it.stringValue == type } ?: UNKNOWN_ERROR
        }
    }
}
