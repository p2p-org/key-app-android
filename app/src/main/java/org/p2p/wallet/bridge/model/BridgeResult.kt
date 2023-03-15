package org.p2p.wallet.bridge.model

sealed interface BridgeResult {

    open class Success<T>(val data: T) : BridgeResult

    sealed class Error : Throwable() {
        object UnableUnwrapContractCall : Error()
        object TransactionGasIsNotSet : Error()
        object EthereumProviderError : Error()
        object ContractError : Error()
        object WalletError : Error()
        object TransactionNotInBundle : Error()
        object NotEnoughAmount : Error()
        object TransactionsNotSigned : Error()
        object TxAndSignatureAreDifferent : Error()
        object UnableToDecodeBytesForRpl : Error()
        object SlippageIsLow : Error()
        object FlashBotError : Error()
        object SignerError : Error()
        object PendingBundleError : Error()
        object InternalServiceError : Error()
    }
}
