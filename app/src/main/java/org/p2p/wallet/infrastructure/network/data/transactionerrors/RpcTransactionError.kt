package org.p2p.wallet.infrastructure.network.data.transactionerrors

import androidx.annotation.Keep

/**
 * https://docs.rs/solana-sdk/latest/solana_sdk/transaction/enum.TransactionError.html
 * sealed class mapped from Solana docs errors
 * to create instance of this class use Gson, we have a type adapter for this class.
 */
@Keep
sealed interface RpcTransactionError {
    object AccountInUse : RpcTransactionError
    object AccountLoadedTwice : RpcTransactionError
    object AccountNotFound : RpcTransactionError
    object ProgramAccountNotFound : RpcTransactionError
    object InsufficientFundsForFee : RpcTransactionError
    object InvalidAccountForFee : RpcTransactionError
    object AlreadyProcessed : RpcTransactionError
    object BlockhashNotFound : RpcTransactionError
    object CallChainTooDeep : RpcTransactionError
    object MissingSignatureForFee : RpcTransactionError
    object InvalidAccountIndex : RpcTransactionError
    object SignatureFailure : RpcTransactionError
    object InvalidProgramForExecution : RpcTransactionError
    object SanitizeFailure : RpcTransactionError
    object ClusterMaintenance : RpcTransactionError
    object AccountBorrowOutstanding : RpcTransactionError
    object WouldExceedMaxBlockCostLimit : RpcTransactionError
    object UnsupportedVersion : RpcTransactionError
    object InvalidWritableAccount : RpcTransactionError
    object WouldExceedMaxAccountCostLimit : RpcTransactionError
    object WouldExceedAccountDataBlockLimit : RpcTransactionError
    object TooManyAccountLocks : RpcTransactionError
    object AddressLookupTableNotFound : RpcTransactionError
    object InvalidAddressLookupTableOwner : RpcTransactionError
    object InvalidAddressLookupTableData : RpcTransactionError
    object InvalidAddressLookupTableIndex : RpcTransactionError
    object InvalidRentPayingAccount : RpcTransactionError
    object WouldExceedMaxVoteCostLimit : RpcTransactionError
    object WouldExceedAccountDataTotalLimit : RpcTransactionError
    object MaxLoadedAccountsDataSizeExceeded : RpcTransactionError

    data class DuplicateInstruction(
        val instructionIndex: Int
    ) : RpcTransactionError

    data class InsufficientFundsForRent(
        val accountIndex: Int
    ) : RpcTransactionError

    data class InstructionError(
        val instructionIndex: Int,
        val instructionErrorType: TransactionInstructionError
    ) : RpcTransactionError {
        /**
         * Returns custom instruction error code if it's custom instruction error
         */
        fun extractCustomErrorCodeOrNull(): Long? =
            (this.instructionErrorType as? TransactionInstructionError.Custom)?.programErrorId
    }

    data class Unknown(
        val originalError: String
    ) : RpcTransactionError
}
