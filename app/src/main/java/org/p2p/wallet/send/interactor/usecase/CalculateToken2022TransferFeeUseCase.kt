package org.p2p.wallet.send.interactor.usecase

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import org.p2p.core.token.Token
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.isZero
import org.p2p.core.utils.toLamports
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig.Companion.transferFeeConfig
import org.p2p.solanaj.rpc.RpcSolanaRepository

class CalculateToken2022TransferFeeUseCase(
    private val getTokenExtensionsUseCase: GetTokenExtensionsUseCase,
    private val solanaRepository: RpcSolanaRepository,
) {

    suspend fun execute(token: Token.Active, tokenAmount: BigDecimal): BigInteger {
        return execute(token, tokenAmount.toLamports(token.decimals))
    }

    suspend fun execute(token: Token.Active, tokenAmount: BigInteger): BigInteger {
        val tokenExtensions = getTokenExtensionsUseCase.execute(token.mintAddress)
        val transferFeeExtension = tokenExtensions.transferFeeConfig
            ?.getActualTransferFee(solanaRepository.getEpochInfo().epoch)
        return transferFeeExtension.calculateFee(tokenAmount)
    }

    private fun AccountInfoTokenExtensionConfig.TransferFeeConfig.TransferFeeConfigData?.calculateFee(
        preFeeAmount: BigInteger
    ): BigInteger {
        if (this == null) return BigInteger.ZERO

        if (transferFeeBasisPoints == 0L || preFeeAmount.isZero()) {
            return BigInteger.ZERO
        }
        val transferFeeBasisPoints = transferFeeBasisPoints.toBigInteger()
        val numerator = preFeeAmount * transferFeeBasisPoints

        // calculate with ceiling rounding mode so that we don't lose any lamports
        val rawFee = numerator.toBigDecimal()
            .divideSafe(BigDecimal("10000"), 6, RoundingMode.CEILING)
            .setScale(0, RoundingMode.CEILING)
            .toBigInteger()

        return minOf(rawFee, maximumFee)
    }
}
