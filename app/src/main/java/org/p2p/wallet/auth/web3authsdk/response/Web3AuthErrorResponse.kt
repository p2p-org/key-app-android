package org.p2p.wallet.auth.web3authsdk.response

import com.google.gson.annotations.SerializedName

data class Web3AuthErrorResponse(
    @SerializedName("name") val errorName: String,
    @SerializedName("code") val errorCode: Int,
    @SerializedName("message") val errorMessage: String,
    @SerializedName("stack") var stack: String?,

    // changes during the mapping
    @Transient val errorType: ErrorType = ErrorType.UNDEFINED
) : Error(errorMessage, Throwable(message = stack)) {
    // https://github.com/p2p-org/web3-auth-sdk-new/blob/main/packages/sdk/src/errors/typeMap.ts
    enum class ErrorType(val code: Int) {
        UNDEFINED(code = -1),
        WRONG_SHARE_TYPE(code = 1001),
        NO_TKEY_INSTANCE(code = 1002),
        INVALID_MNEMONIC(code = 1003),
        NO_TOKEN_PROVIDED(code = 1004),
        NO_DEVICE_SHARE(code = 1005),
        CUSTOM_SHARE_EXIST(code = 1006),
        BAD_MAC(code = 1007),
        HEX_TO_BIN_CONVERSION(code = 1008),
        CANNOT_RECONSTRUCT(code = 1009),
        CANNOT_READ_WEB_STORAGE(code = 1011),
        CANNOT_INITIALIZE_TKEY(code = 1012),
        DUPLICATE_TOKEN(code = 1013),
        READ_VERSION_ERROR(code = 1014),
        NO_CUSTOM_SHARE(code = 1015),
        USER_NOT_REGISTERED(code = 1016),
        NO_CUSTOM_METADATA(code = 1017),
        INVALID_INIT_ARGS(code = 1018),
        NETWORK_ERROR(code = 1666);

        companion object {
            fun findByCode(code: Int): ErrorType = values().find { it.code == code } ?: UNDEFINED
        }
    }
}
