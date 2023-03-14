package org.p2p.wallet.bridge.api.mapper

import org.p2p.wallet.bridge.model.BridgeResult

class BridgeServiceErrorMapper {

    fun parseError(errorCode: Int): BridgeResult.Error {
        return when (errorCode) {
            32001 -> BridgeResult.Error.UnableUnwrapContractCall
            32002 -> BridgeResult.Error.TransactionGasIsNotSet
            32003 -> BridgeResult.Error.EthereumProviderError
            32004 -> BridgeResult.Error.ContractError
            32005 -> BridgeResult.Error.WalletError
            32006 -> BridgeResult.Error.TransactionNotInBundle
            32007 -> BridgeResult.Error.NotEnoughAmount
            32008 -> BridgeResult.Error.TransactionsNotSigned
            32009 -> BridgeResult.Error.TxAndSignatureAreDifferent
            32010 -> BridgeResult.Error.UnableToDecodeBytesForRpl
            32011 -> BridgeResult.Error.SlippageIsLow
            33001 -> BridgeResult.Error.FlashBotError
            33002 -> BridgeResult.Error.SignerError
            33003 -> BridgeResult.Error.PendingBundleError
            else -> BridgeResult.Error.InternalServiceError
        }
    }
}
