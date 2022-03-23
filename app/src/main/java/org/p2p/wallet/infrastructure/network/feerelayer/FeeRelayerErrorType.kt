package org.p2p.wallet.infrastructure.network.feerelayer

enum class FeeRelayerErrorType(val stringValue: String) {
    PARSE_HASH_ERROR("ParseHashError"),
    PARSE_PUBKEY_ERROR("ParsePubkeyError"),
    PARSE_KEYPAIR_ERROR("ParseKeypairError"),
    PARSE_SIGNATURE_ERROR("ParseSignatureError"),
    WRONG_SIGNATURE("WrongSignature"),
    SIGNER_ERROR("SignerError"),
    CLIENT_ERROR("ClientError"),
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

    UNKNOWN("UnknownError");
}
