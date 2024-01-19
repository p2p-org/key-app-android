package org.p2p.wallet.send.interactor.usecase

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig.Companion.getInterestBearingConfig
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig.Companion.getTransferFeeConfig
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.model.send_service.GeneratedTransactionFees
import org.p2p.wallet.send.model.send_service.SendFeePayerMode
import org.p2p.wallet.send.model.send_service.SendRentPayerMode
import org.p2p.wallet.send.model.send_service.SendTransferMode
import org.p2p.wallet.send.repository.SendServiceRepository

/**
 * This use case is used to calculate fees for sending tokens
 * By default, send-service calculates network + account creation fees in "mint" token if specified
 * or in SOL if not specified.
 * Token2022 transfer fee is also included into result, but fees are always in "mint" token
 *
 * [GeneratedTransactionFees.Result.tokenAccountRent] might be null if there is no account creation fee
 * [GeneratedTransactionFees.Result.token2022TransferFeePercent] might be null if there is no transfer fee
 *
 * Also one notice: DO NOT use [SendFeePayerMode.Service] or [SendRentPayerMode.Service] for calculating fees,
 * because it will decrement left free transactions count
 *
 * Example:
 *   Send BERN where fee payer token is SOL
 *   Result fees:
 *      - network fee: 0.000005 SOL
 *      - account creation fee: 0.000005 SOL
 *      - token2022 transfer fee: 0.000005 BERN
 *
 *  Send BERN where fee payer token is USDC
 *   Result fees:
 *      - network fee: 0.000005 USDC
 *      - account creation fee: 0.000005 USDC
 *      - token2022 transfer fee: 0.000005 BERN
 *
 *   Send BERN where fee payer token is BERN
 *   Result fees:
 *      - network fee: 0.000005 BERN
 *      - account creation fee: 0.000005 BERN
 *      - token2022 transfer fee: 0.000005 BERN
 *
 * @see [SendServiceRepository.generateTransaction]
 * @see [GeneratedTransactionFees.Result]
 */
class CalculateSendServiceFeesUseCase(
    private val dispatchers: CoroutineDispatchers,
    private val sendServiceRepository: SendServiceRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val getTokenExtensionsUseCase: GetTokenExtensionsUseCase,
    private val solanaRepository: RpcSolanaRepository,
) {

    suspend fun execute(
        feePayerToken: Token.Active,
        sourceToken: Token.Active,
        amountLamports: BigInteger,
        recipient: Base58String,
        transferMode: SendTransferMode = SendTransferMode.ExactOut,
    ): GeneratedTransactionFees = withContext(dispatchers.io) {
        val sender = tokenKeyProvider.publicKey
        try {
            val feePayer = if (feePayerToken.isSOL) {
                SendFeePayerMode.UserSol to null
            } else {
                SendFeePayerMode.Custom to feePayerToken.mintAddress.toBase58Instance()
            }
            val rentPayer = if (feePayerToken.isSOL) {
                SendRentPayerMode.UserSol to null
            } else {
                SendRentPayerMode.Custom to feePayerToken.mintAddress.toBase58Instance()
            }
            val tx = sendServiceRepository.generateTransaction(
                userWallet = sender.toBase58Instance(),
                amountLamports = amountLamports,
                recipient = recipient,
                tokenMint = if (sourceToken.isSOL) null else sourceToken.mintAddress.toBase58Instance(),
                transferMode = transferMode,
                feePayerMode = feePayer.first,
                customFeePayerTokenMint = feePayer.second,
                rentPayerMode = rentPayer.first,
                customRentPayerTokenMint = rentPayer.second,
            )

            val tokenExtensions = getTokenExtensionsUseCase.execute(sourceToken)

            GeneratedTransactionFees.Result(
                recipientGetsAmount = tx.recipientGetsAmount,
                totalAmount = tx.totalAmount,
                networkFee = tx.networkFee.amount,
                tokenAccountRent = tx.tokenAccountRent.amount,
                token2022TransferFee = tx.token2022TransferFee.amount,
                token2022TransferFeePercent = tokenExtensions
                    .getTransferFeeConfig()
                    ?.getActualTransferFee(solanaRepository.getEpochInfo(true).epoch)
                    ?.transferFeePercent,
                token2022InterestBearingPercent = tokenExtensions
                    .getInterestBearingConfig()
                    ?.currentRate
                    ?.toBigDecimal()
            )
        } catch (e: Throwable) {
            Timber.e(e, "Unable to calculate fees")
            GeneratedTransactionFees.Error(e)
        }
    }
}
