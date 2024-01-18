package org.p2p.wallet.send.interactor.usecase

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.isZero
import org.p2p.core.utils.toLamports
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig.Companion.getTransferFeeConfig
import org.p2p.solanaj.rpc.RpcSolanaRepository

class CalculateToken2022TransferFeeUseCase(
    private val getTokenExtensionsUseCase: GetTokenExtensionsUseCase,
    private val solanaRepository: RpcSolanaRepository,
) {

    suspend fun execute(token: Token.Active, tokenAmount: BigDecimal): BigInteger {
        return execute(token, tokenAmount.toLamports(token.decimals))
    }

    suspend fun execute(token: Token.Active, tokenAmount: BigInteger): BigInteger {
        val tokenExtensions = getTokenExtensionsUseCase.execute(token)
        val transferFeeExtension = tokenExtensions.getTransferFeeConfig()
            ?.getActualTransferFee(solanaRepository.getEpochInfo().epoch)
        return transferFeeExtension.calculateFee(tokenAmount)
    }

    private fun AccountInfoTokenExtensionConfig.TransferFeeConfig.TransferFeeConfigData?.calculateFee(
        preFeeAmount: BigInteger
    ): BigInteger {
        if (this == null) return BigInteger.ZERO

        if (transferFeeBasisPoints == 0 || preFeeAmount.isZero()) {
            return BigInteger.ZERO
        }
        val transferFeeBasisPoints = transferFeeBasisPoints.toBigInteger()
        val numerator = preFeeAmount * transferFeeBasisPoints
        val rawFee = numerator.divideSafe(BigInteger("10000"))

        return minOf(rawFee, maximumFee)
    }
}
