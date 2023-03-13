package org.p2p.wallet.claim.api.mapper

import org.p2p.wallet.claim.api.response.BridgeErrorResponse
import org.p2p.wallet.claim.model.BridgeServiceError

class BridgeServiceErrorMapper {

    fun fromNetwork(error: BridgeErrorResponse): BridgeServiceError {
        return when (error.errorCode) {
            32001 -> BridgeServiceError.UnableUnwrapContractCall
            32002 -> BridgeServiceError.TransactionGasIsNotSet
            32003 -> BridgeServiceError.EthereumProviderError
            32004 -> BridgeServiceError.ContractError
            32005 -> BridgeServiceError.WalletError
            32006 -> BridgeServiceError.TransactionNotInBundle
            32007 -> BridgeServiceError.NotEnoughAmount
            32008 -> BridgeServiceError.TransactionsNotSigned
            32009 -> BridgeServiceError.TxAndSignatureAreDifferent
            32010 -> BridgeServiceError.UnableToDecodeBytesForRpl
            32011 -> BridgeServiceError.SlippageIsLow
            33001 -> BridgeServiceError.FlashBotError
            33002 -> BridgeServiceError.SignerError
            33003 -> BridgeServiceError.PendingBundleError
            else -> BridgeServiceError.InternalServiceError
        }
    }
}
