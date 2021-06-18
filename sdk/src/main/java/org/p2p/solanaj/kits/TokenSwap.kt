package org.p2p.solanaj.kits

import org.p2p.solanaj.kits.Pool.PoolInfo
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionRequest
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.TokenAccountBalance
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
        pool: PoolInfo,
        slippage: Double,
        amountIn: BigInteger,
        balanceA: TokenAccountBalance,
        balanceB: TokenAccountBalance,
        wrappedSolAccount: PublicKey,
        accountAddressA: PublicKey?,
        accountAddressB: PublicKey?,
        getAccountInfo: suspend (PublicKey) -> AccountInfo,
        getBalanceNeeded: suspend (Long) -> Long,
        sendTransaction: suspend (transaction: TransactionRequest, signers: List<Account>) -> String
    ): String {
        val signers = ArrayList(listOf(owner))
        val transaction = TransactionRequest()

        // swap type
        val source = pool.tokenAccountA
        val tokenA = if (source.equals(pool.tokenAccountA)) pool.tokenAccountA else pool.tokenAccountB
        val isTokenAEqTokenAccountA = tokenA.equals(pool.tokenAccountA)
        val tokenB = if (isTokenAEqTokenAccountA) pool.tokenAccountB else pool.tokenAccountA
        val mintA = if (isTokenAEqTokenAccountA) pool.mintA else pool.mintB
        val mintB = if (isTokenAEqTokenAccountA) pool.mintB else pool.mintA

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
        var toAccount = accountAddressB
        val isWrappedSol = mintB.equals(wrappedSolAccount)
        if (toAccount == null) {
            val newAccount = Account()
            val newAccountPubKey = newAccount.publicKey
            val createAccountInstruction = SystemProgram.createAccount(
                owner.publicKey,
                newAccountPubKey,
                balanceNeeded,
                AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong(),
                TokenProgram.PROGRAM_ID
            )
            val initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                newAccountPubKey,
                mintB,
                owner.publicKey
            )
            transaction.addInstruction(createAccountInstruction)
            transaction.addInstruction(initializeAccountInstruction)
            signers.add(newAccount)
            toAccount = newAccountPubKey
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
            toAccount,
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
            closeAccountPublicKey = toAccount
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
        return sendTransaction.invoke(transaction, signers)
    }

    companion object {
        fun calculateSwapMinimumReceiveAmount(estimatedAmount: BigInteger, slippage: Double): BigInteger {
            return BigDecimal.valueOf(estimatedAmount.toDouble() * (1 - slippage)).toBigInteger()
        }

        fun calculateSwapEstimatedAmount(
            tokenABalance: TokenAccountBalance,
            tokenBBalance: TokenAccountBalance,
            inputAmount: BigInteger
        ): BigInteger {
            return tokenBBalance.amount.multiply(inputAmount).divide(tokenABalance.amount.add(inputAmount))
        }
    }
}