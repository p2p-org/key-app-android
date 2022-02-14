package org.p2p.wallet.swap.interactor.orca

import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.utils.toLamports
import java.math.BigDecimal

class OrcaExecuteInteractor(
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val environmentManager: EnvironmentManager

) {

    // Execute swap
    suspend fun swap(
        feePayerToken: Token.Active,
        fromToken: Token.Active,
        toToken: Token,
        bestPoolsPair: OrcaPoolsPair,
        amount: Double,
        slippage: Double
    ): OrcaSwapResult {
        val fromDecimals = bestPoolsPair[0].tokenABalance?.decimals
            ?: throw IllegalStateException("Invalid pool")

        val lamports = BigDecimal(amount).toLamports(fromDecimals)

        val feeRelayerProgramId = FeeRelayerProgram.getProgramId(environmentManager.isMainnet())
//        val preparedTransaction = feeRelayerInteractor.topUpAndSwap(
//            feeRelayerProgramId = feeRelayerProgramId,
//            sourceToken = TokenInfo(fromToken.publicKey, fromToken.mintAddress),
//            destinationAddress = toToken.publicKey,
//            payingFeeToken = TokenInfo(feePayerToken.publicKey, feePayerToken.mintAddress),
//            inputAmount = lamports,
//            destinationTokenMint = toToken.mintAddress,
//            swapPools = bestPoolsPair,
//            slippage = slippage
//        )
//
//        val transactionId = feeRelayerInteractor.topUpAndRelayTransaction(
//            feeRelayerProgramId = feeRelayerProgramId,
//            preparedTransaction = preparedTransaction,
//            payingFeeToken = TokenInfo(feePayerToken.publicKey, feePayerToken.mintAddress)
//        ).firstOrNull().orEmpty()

        // fixme: find address
        return OrcaSwapResult.Finished("transactionId", toToken.publicKey.orEmpty())
    }
}