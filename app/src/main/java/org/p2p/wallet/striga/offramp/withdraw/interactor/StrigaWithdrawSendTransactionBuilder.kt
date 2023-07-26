package org.p2p.wallet.striga.offramp.withdraw.interactor

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.utils.toPublicKey

class StrigaWithdrawSendTransactionBuilder(
    private val addressInteractor: TransactionAddressInteractor,
    private val amountRepository: RpcAmountRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val transactionInteractor: TransactionInteractor,
) {
    suspend fun buildSendTransaction(
        userAccount: Account,
        destinationAddress: PublicKey,
        token: Token.Active,
        amountToSend: BigInteger,
    ): PreparedTransaction {
        val splDestinationAddress = addressInteractor.findSplTokenAddressData(
            destinationAddress = destinationAddress,
            mintAddress = token.mintAddress
        )
        if (splDestinationAddress.shouldCreateAccount) {
            error("SPL token account should be created by Striga")
        }

        val instructions = mutableListOf<TransactionInstruction>()

        val accountCreationFee = amountRepository.getMinBalanceForRentExemption(
            TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
        )

        instructions += TokenProgram.createTransferCheckedInstruction(
            /* tokenProgramId = */ TokenProgram.PROGRAM_ID,
            /* source = */ userAccount.publicKey,
            /* mint = */ token.mintAddress.toPublicKey(),
            /* destination = */ splDestinationAddress.destinationAddress,
            /* owner = */ userAccount.publicKey,
            /* amount = */ amountToSend,
            /* decimals = */ token.decimals
        )

        val feePayer = feeRelayerAccountInteractor.getFeePayerPublicKey()

        return transactionInteractor.prepareTransaction(
            instructions = instructions,
            signers = listOf(userAccount),
            feePayer = feePayer,
            accountsCreationFee = accountCreationFee
        )
    }
}
