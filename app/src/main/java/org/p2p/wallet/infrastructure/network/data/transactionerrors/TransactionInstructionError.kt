package org.p2p.wallet.infrastructure.network.data.transactionerrors

import io.realm.internal.Keep

/**
 * https://docs.rs/solana-sdk/latest/solana_sdk/instruction/enum.InstructionError.html
 *  [3, { "Custom": 6022 }]
 *  [3, "InvalidStuff"]
 */
@Keep
sealed interface TransactionInstructionError {
    object GenericError : TransactionInstructionError
    object InvalidArgument : TransactionInstructionError
    object InvalidInstructionData : TransactionInstructionError
    object InvalidAccountData : TransactionInstructionError
    object AccountDataTooSmall : TransactionInstructionError
    object InsufficientFunds : TransactionInstructionError
    object IncorrectProgramId : TransactionInstructionError
    object MissingRequiredSignature : TransactionInstructionError
    object AccountAlreadyInitialized : TransactionInstructionError
    object UninitializedAccount : TransactionInstructionError
    object UnbalancedInstruction : TransactionInstructionError
    object ModifiedProgramId : TransactionInstructionError
    object ExternalAccountLamportSpend : TransactionInstructionError
    object ExternalAccountDataModified : TransactionInstructionError
    object ReadonlyLamportChange : TransactionInstructionError
    object ReadonlyDataModified : TransactionInstructionError
    object DuplicateAccountIndex : TransactionInstructionError
    object ExecutableModified : TransactionInstructionError
    object RentEpochModified : TransactionInstructionError
    object NotEnoughAccountKeys : TransactionInstructionError
    object AccountDataSizeChanged : TransactionInstructionError
    object AccountNotExecutable : TransactionInstructionError
    object AccountBorrowFailed : TransactionInstructionError
    object AccountBorrowOutstanding : TransactionInstructionError
    object DuplicateAccountOutOfSync : TransactionInstructionError
    object InvalidError : TransactionInstructionError
    object ExecutableDataModified : TransactionInstructionError
    object ExecutableLamportChange : TransactionInstructionError
    object ExecutableAccountNotRentExempt : TransactionInstructionError
    object UnsupportedProgramId : TransactionInstructionError
    object CallDepth : TransactionInstructionError
    object MissingAccount : TransactionInstructionError
    object ReentrancyNotAllowed : TransactionInstructionError
    object MaxSeedLengthExceeded : TransactionInstructionError
    object InvalidSeeds : TransactionInstructionError
    object InvalidRealloc : TransactionInstructionError
    object ComputationalBudgetExceeded : TransactionInstructionError
    object PrivilegeEscalation : TransactionInstructionError
    object ProgramEnvironmentSetupFailure : TransactionInstructionError
    object ProgramFailedToComplete : TransactionInstructionError
    object ProgramFailedToCompile : TransactionInstructionError
    object Immutable : TransactionInstructionError
    object IncorrectAuthority : TransactionInstructionError
    object AccountNotRentExempt : TransactionInstructionError
    object InvalidAccountOwner : TransactionInstructionError
    object ArithmeticOverflow : TransactionInstructionError
    object UnsupportedSysvar : TransactionInstructionError
    object IllegalOwner : TransactionInstructionError
    object MaxAccountsDataAllocationsExceeded : TransactionInstructionError
    object MaxAccountsExceeded : TransactionInstructionError
    object MaxInstructionTraceLengthExceeded : TransactionInstructionError

    data class BorshIoError(val error: String) : TransactionInstructionError
    data class Custom(val programErrorId: Long) : TransactionInstructionError
    data class Unknown(val name: String) : TransactionInstructionError
}
