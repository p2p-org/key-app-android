package org.p2p.wallet.striga.model

import com.google.gson.annotations.SerializedName

/**
 * @property errorCode parsed error code or null if it's not parsed
 */
data class StrigaApiErrorResponse(
    @SerializedName("status")
    val httpStatus: Int,
    @SerializedName("errorCode")
    private val internalErrorCode: StrigaApiErrorCode?,
    @SerializedName("errorDetails")
    val details: String?,
) {
    val errorCode: StrigaApiErrorCode get() = internalErrorCode ?: StrigaApiErrorCode.UNKNOWN
}

/**
 * https://docs.striga.com/reference/error-codes-for-reference-only
 */
enum class StrigaApiErrorCode(val code: String) {
    @SerializedName("30009")
    KYC_PENDING_REVIEW("30009"),

    @SerializedName("30041")
    MOBILE_ALREADY_EXISTS("30041"),

    @SerializedName("30044")
    MOBILE_ALREADY_VERIFIED("30044"),

    @SerializedName("30031")
    INVALID_VERIFICATION_CODE("30031"),

    @SerializedName("30003")
    EXCEEDED_VERIFICATION_ATTEMPTS("30003"),

    @SerializedName("31009")
    USER_MOBILE_NUMBER_ALREADY_VERIFIED("31009"),

    @SerializedName("30005")
    USER_DOES_NOT_EXIST("30005"),

    @SerializedName("31008")
    EXCEEDED_DAILY_RESEND_SMS_LIMIT("31008"),

    // Wallet errors
    @SerializedName("00013")
    ADDRESS_ALREADY_WHITELISTED("00013"),

    @SerializedName("41004")
    ADDRESS_NOT_WHITELISTED("41004"),

    @SerializedName("60001")
    TOO_LARGE_AMOUNT("60001"),

    @SerializedName("41008")
    TOO_SMALL_AMOUNT("41008"),

    @SerializedName("31004")
    INSUFFICIENT_BALANCE("31004"),

    @SerializedName("41005")
    INVALID_DESTINATION_ADDRESS("41005"),

    UNKNOWN("-1")
}
