package org.p2p.wallet.infrastructure.network.feerelayer

enum class ErrorType(val stringValue: String) {
    parseHashError("ParseHashError"),
    parsePubkeyError("ParsePubkeyError"),
    parseKeypairError("ParseKeypairError"),
    parseSignatureError("ParseSignatureError"),
    wrongSignature("WrongSignature"),
    signerError("SignerError"),
    clientError("ClientError"),
    programError("ProgramError"),
    tooSmallAmount("TooSmallAmount"),
    notEnoughBalance("NotEnoughBalance "),
    notEnoughTokenBalance("NotEnoughTokenBalance"),
    decimalsMismatch("DecimalsMismatch"),
    tokenAccountNotFound("TokenAccountNotFound"),
    incorrectAccountOwner("IncorrectAccountOwner"),
    tokenMintMismatch("TokenMintMismatch"),
    unsupportedRecipientAddress("UnsupportedRecipientAddress"),
    feeCalculatorNotFound("FeeCalculatorNotFound"),
    notEnoughOutAmount("NotEnoughOutAmount"),
    unknownSwapProgramId("UnknownSwapProgramId"),

    unknown("UnknownError");
}
