package org.p2p.wallet.claim.model

sealed class BridgeServiceError {

    object UnableUnwrapContractCall : BridgeServiceError()
    object TransactionGasIsNotSet : BridgeServiceError()
    object EthereumProviderError : BridgeServiceError()
    object ContractError : BridgeServiceError()
    object WalletError : BridgeServiceError()
    object TransactionNotInBundle : BridgeServiceError()
    object NotEnoughAmount : BridgeServiceError()
    object TransactionsNotSigned : BridgeServiceError()
    object TxAndSignatureAreDifferent : BridgeServiceError()
    object UnableToDecodeBytesForRpl : BridgeServiceError()
    object SlippageIsLow : BridgeServiceError()
    object FlashBotError : BridgeServiceError()
    object SignerError : BridgeServiceError()
    object PendingBundleError : BridgeServiceError()
    object InternalServiceError : BridgeServiceError()
}
