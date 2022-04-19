package org.p2p.wallet.infrastructure.network.feerelayer

enum class FeeRelayerErrorType(val stringValue: String) {
    PARSE_HASH_ERROR("ParseHashError"),
    PARSE_PUBKEY_ERROR("ParsePubkeyError"),
    PROGRAM_ERROR("ProgramError"),
    TOO_SMALL_AMOUNT("TooSmallAmount"),
    NOT_ENOUGH_BALANCE("NotEnoughBalance "),
    NOT_ENOUGH_TOKEN_BALANCE("NotEnoughTokenBalance"),
    DECIMALS_MISMATCH("DecimalsMismatch"),
    TOKEN_ACCOUNT_NOT_FOUND("TokenAccountNotFound"),
    INCORRECT_ACCOUNT_OWNER("IncorrectAccountOwner"),
    TOKEN_MINT_MISMATCH("TokenMintMismatch"),
    UNSUPPORTED_RECIPIENT_ADDRESS("UnsupportedRecipientAddress"),
    FEE_CALCULATOR_NOT_FOUND("FeeCalculatorNotFound"),
    NOT_ENOUGH_OUT_AMOUNT("NotEnoughOutAmount"),
    UNKNOWN_SWAP_PROGRAM_ID("UnknownSwapProgramId"),
    SWAP_AUTHORITIES_DO_NOT_MATCH("SwapAuthoritiesDoNotMatch"),
    SWAP_METHODS_DO_NOT_MATCH("SwapMethodsDoNotMatch"),
    UNSUPPORTED_SWAP_METHOD("UnsupportedSwapMethod"),
    WRONG_SOURCE_TOKEN_ACCOUNT("WrongSourceTokenAccount"),
    FREE_FEE_LIMIT_EXCEEDED("FreeFeeLimitExceeded"),
    ACCESS_DATA_ERROR("AccessDataError"),

    SLIPPAGE_LIMIT("SlippageLimit"),
    INSUFFICIENT_FUNDS("InsufficientFunds"),
    MAXIMUM_NUMBER_OF_INSTRUCTIONS_ALLOWED_EXCEEDED("MaximumNumberOfInstructionsAllowedExceeded"),

    UNKNOWN("UnknownError");
}
