package org.p2p.wallet.swap.repository

import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData
import org.p2p.solanaj.programs.TokenSwapProgram
import org.p2p.solanaj.rpc.RpcException
import java.math.BigDecimal
import java.math.BigInteger
import java.util.ArrayList

class TokenSwap {

    @Throws(RpcException::class)
    suspend fun swap(
        owner: Account,
        pool: OrcaPool,
        slippage: Double,
        amountIn: BigInteger,
        balanceA: AccountBalance,
        balanceB: AccountBalance,
        wrappedSolAccount: PublicKey,
        accountAddressA: PublicKey?,
        associatedAddress: PublicKey,
        shouldCreateAssociatedInstruction: Boolean,
        getAccountInfo: suspend (PublicKey) -> AccountInfo?,
        getBalanceNeeded: suspend (Long) -> Long,
        getRecentBlockhash: suspend () -> String,
        sendTransaction: suspend (transaction: Transaction) -> String
    ): String {
        val signers = ArrayList(listOf(owner))
        val transaction = Transaction()

        // swap type
        val source = pool.tokenAccountA
        val tokenA = if (source.equals(pool.tokenAccountA)) pool.tokenAccountA else pool.tokenAccountB
        val isTokenAEqTokenAccountA = tokenA.equals(pool.tokenAccountA)
        val tokenB = if (isTokenAEqTokenAccountA) pool.tokenAccountB else pool.tokenAccountA
        val mintA = if (isTokenAEqTokenAccountA) pool.sourceMint else pool.destinationMint
        val mintB = if (isTokenAEqTokenAccountA) pool.destinationMint else pool.sourceMint

        val accountInfo = getAccountInfo.invoke(tokenA)
        val tokenAInfo = TokenTransaction.getAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        val space = AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
        val balanceNeeded = getBalanceNeeded.invoke(space.toLong())

        val fromAccount: PublicKey? = if (tokenAInfo.isNative) {
            val newAccount = Account()
            val newAccountPubKey = newAccount.publicKey
            val createAccountInstruction = SystemProgram.createAccount(
                owner.publicKey,
                newAccountPubKey,
                amountIn.toLong() + balanceNeeded,
                AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong(),
                TokenProgram.PROGRAM_ID
            )
            val initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                newAccountPubKey,
                wrappedSolAccount,
                owner.publicKey
            )
            transaction.addInstruction(createAccountInstruction)
            transaction.addInstruction(initializeAccountInstruction)
            signers.add(newAccount)
            newAccountPubKey
        } else {
            accountAddressA
        }
        val isWrappedSol = mintB.equals(wrappedSolAccount)

        if (shouldCreateAssociatedInstruction) {
            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                mintB,
                associatedAddress,
                owner.publicKey,
                owner.publicKey
            )

            transaction.addInstruction(createAccount)
        }

        val userTransferAuthority = Account()
        val approve = TokenProgram.approveInstruction(
            TokenProgram.PROGRAM_ID,
            fromAccount,
            userTransferAuthority.publicKey,
            owner.publicKey,
            amountIn
        )
        val estimatedAmount = calculateSwapEstimatedAmount(
            balanceA,
            balanceB,
            amountIn
        )
        val minimumAmountOut = calculateSwapMinimumReceiveAmount(
            estimatedAmount,
            slippage
        )
        val swap = TokenSwapProgram.swapInstruction(
            pool.address,
            pool.authority,
            userTransferAuthority.publicKey,
            fromAccount,
            tokenA,
            tokenB,
            associatedAddress,
            pool.tokenPool,
            pool.feeAccount,
            pool.feeAccount,
            TokenProgram.PROGRAM_ID,
            pool.swapProgramId,
            amountIn,
            minimumAmountOut
        )
        transaction.addInstruction(approve)
        transaction.addInstruction(swap)
        val isNeedCloseAccount = tokenAInfo.isNative || isWrappedSol
        var closeAccountPublicKey: PublicKey? = null
        if (tokenAInfo.isNative) {
            closeAccountPublicKey = fromAccount
        } else if (isWrappedSol) {
            closeAccountPublicKey = associatedAddress
        }
        if (isNeedCloseAccount && closeAccountPublicKey != null) {
            val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                closeAccountPublicKey,
                owner.publicKey,
                owner.publicKey
            )
            transaction.addInstruction(closeAccountInstruction)
        }
        signers.add(userTransferAuthority)
        transaction.setRecentBlockHash(getRecentBlockhash())
        transaction.sign(signers)
        return sendTransaction.invoke(transaction)
    }

    companion object {
        fun calculateSwapMinimumReceiveAmount(estimatedAmount: BigInteger, slippage: Double): BigInteger {
            return BigDecimal.valueOf(estimatedAmount.toDouble() * (1 - slippage)).toBigInteger()
        }

        fun calculateSwapEstimatedAmount(
            tokenABalance: AccountBalance,
            tokenBBalance: AccountBalance,
            inputAmount: BigInteger
        ): BigInteger {
            return tokenBBalance.amount.multiply(inputAmount).divide(tokenABalance.amount.add(inputAmount))
        }
    }
}