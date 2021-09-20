package com.p2p.wallet.swap.interactor

import com.p2p.wallet.main.model.Token
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.interactor.SerumSwapInteractor.Companion.BASE_TAKER_FEE_BPS
import com.p2p.wallet.swap.interactor.SerumSwapInteractor.Companion.FEE_MULTIPLIER
import com.p2p.wallet.swap.model.FeeType
import com.p2p.wallet.swap.model.SwapFee
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.toLamports
import org.p2p.solanaj.programs.TokenProgram
import java.math.BigDecimal
import java.math.BigInteger

class SwapInteractor(
    private val rpcRepository: RpcRepository,
    private val serumSwapInteractor: SerumSwapInteractor
) {

    fun isFeeRelayerEnabled(
        source: Token?,
        destination: Token?
    ): Boolean {
        // TODO: - Later
        return false
    }

    suspend fun calculateFees(
        sourceToken: Token?,
        destinationToken: Token?,
        lamportsPerSignature: BigInteger?,
        creatingAccountFee: BigInteger?
    ): Map<FeeType, SwapFee> {
        val fees = mutableMapOf<FeeType, SwapFee>()
        val liquidityFee = "${calculateLiquidityProviderFee()} %"
        fees[FeeType.LIQUIDITY_PROVIDER] = SwapFee(stringValue = liquidityFee)

        if (sourceToken == null ||
            destinationToken == null ||
            lamportsPerSignature == null ||
            creatingAccountFee == null
        ) return fees

        val isFeeRelayerEnabled = isFeeRelayerEnabled(sourceToken, destinationToken)

        val networkFee = serumSwapInteractor.calculateNetworkFee(
            fromWallet = sourceToken,
            toWallet = destinationToken,
            lamportsPerSignature = lamportsPerSignature,
            minRentExemption = creatingAccountFee
        )

        if (!isFeeRelayerEnabled) {
            fees[FeeType.DEFAULT] = SwapFee("SOL", networkFee, "")
            return fees
        }

        // if paying directly with SOL
//        if (isFeeRelayerEnabled) {
//            fees[FeeType.DEFAULT] = SwapFee("SOL", networkFee)
//            return fees
//        }

        // convert fee from SOL to amount in source token
        // TODO: - Check: look for sourceToken/SOL price and send to fee-relayer
        val fair = serumSwapInteractor.loadFair(sourceToken.mintAddress, Token.WRAPPED_SOL_MINT)
        val neededAmount = calculateNeededInputAmount(
            forReceivingEstimatedAmount = networkFee.fromLamports(),
            rate = fair.toBigDecimal()
        )

        val lamports = neededAmount?.toLamports(sourceToken.decimals)

        return if (lamports == null) {
            emptyMap()
        } else {
            fees[FeeType.DEFAULT] = SwapFee(sourceToken.tokenSymbol, lamports, "")
            fees
        }
    }

    // / Estimated amount that user can get after swapping
    fun calculateEstimatedAmount(
        inputAmount: Double?,
        rate: Double?,
        slippage: Double?
    ): BigDecimal? {
        return if (inputAmount != null && rate != null && rate != 0.0) {
            BigDecimal(FEE_MULTIPLIER * (inputAmount / rate))
        } else {
            null
        }
    }

    // / Input amount needed for receiving an estimated amount
    fun calculateNeededInputAmount(
        forReceivingEstimatedAmount: BigDecimal?,
        rate: BigDecimal?
    ): BigDecimal? {
        return if (forReceivingEstimatedAmount != null && rate != null && !rate.isZero()) {
            forReceivingEstimatedAmount * rate / FEE_MULTIPLIER.toBigDecimal()
        } else {
            null
        }
    }

    // / Maximum amount that user can use for swapping
    fun calculateAvailableAmount(
        sourceToken: Token?,
        fee: SwapFee?
    ): BigDecimal? {
        if (sourceToken == null || sourceToken.total.isZero()) {
            return null
        }

        if (fee == null) return sourceToken.total

        var amount = sourceToken.total.toLamports(sourceToken.decimals)

        if (fee.tokenSymbol == "SOL") {
            if (sourceToken.isSOL) {
                if (amount > fee.lamports) {
                    amount -= fee.lamports
                } else {
                    amount = BigInteger.ZERO
                }
            }
        } else if (fee.tokenSymbol == sourceToken.tokenSymbol) {
            if (amount > fee.lamports) {
                amount -= fee.lamports
            } else {
                amount = BigInteger.ZERO
            }
        }

        return amount.fromLamports(sourceToken.decimals)
    }

    fun calculateLiquidityProviderFee(): BigDecimal = (BASE_TAKER_FEE_BPS * 100).toBigDecimal()

    suspend fun loadPrice(fromMint: String, toMint: String): BigDecimal =
        serumSwapInteractor.loadFair(fromMint, toMint).toBigDecimal()

    suspend fun getLamportsPerSignature(): BigInteger = rpcRepository.getFees(null)

    suspend fun getCreatingTokenAccountFee(): BigInteger =
        rpcRepository
            .getMinimumBalanceForRentExemption(TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong())
            .toBigInteger()
}