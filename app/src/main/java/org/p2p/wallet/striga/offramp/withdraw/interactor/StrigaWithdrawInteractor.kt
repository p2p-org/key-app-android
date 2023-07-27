package org.p2p.wallet.striga.offramp.withdraw.interactor

import timber.log.Timber
import java.math.BigDecimal
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.toLamports
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.model.types.ConfirmationStatus
import org.p2p.wallet.feerelayer.interactor.FeeRelayerViaLinkInteractor
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.toPublicKey

class StrigaWithdrawInteractor(
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val userTokensInteractor: UserTokensInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val feeRelayerLinkInteractor: FeeRelayerViaLinkInteractor,
    private val transactionBuilder: StrigaWithdrawSendTransactionBuilder
) {

    suspend fun getUserEurBankingDetails(): StrigaUserBankingDetails {
        return strigaWalletInteractor.getEurBankingDetails()
    }

    suspend fun saveUpdatedEurBankingDetails(iban: String, bic: String) {
        strigaWalletInteractor.saveNewEurBankingDetails(iban, bic)
    }

    suspend fun withdrawEur() {
        // no-op for now
    }

    suspend fun withdrawUsdc(amount: BigDecimal): Base58String {
        feeRelayerLinkInteractor.load()

        val userUsdcToken = userTokensInteractor.findUserToken(Constants.USDC_MINT)
            ?: throw StrigaWithdrawError.StrigaSendUsdcFailed("USDC token not found")
        val userStrigaUsdcAddress = strigaWalletInteractor.getCryptoAccountDetails().depositAddress

        return sendUsdcToStriga(userUsdcToken, userStrigaUsdcAddress, amount)
    }

    @Throws(StrigaWithdrawError.StrigaSendUsdcFailed::class)
    private suspend fun sendUsdcToStriga(
        usdcToken: Token.Active,
        strigaAddress: String,
        amountToSend: BigDecimal
    ): Base58String {
        val userAccount = Account(tokenKeyProvider.keyPair)

        val txSignature = try {
            val preparedTransaction = transactionBuilder.buildSendTransaction(
                userAccount = userAccount,
                destinationAddress = strigaAddress.toPublicKey(),
                token = usdcToken,
                amountToSend = amountToSend.toLamports(usdcToken.decimals)
            )

            feeRelayerLinkInteractor.signAndSendTransaction(
                preparedTransaction = preparedTransaction,
                statistics = FeeRelayerStatistics(
                    operationType = OperationType.TRANSFER,
                    currency = usdcToken.mintAddress
                ),
                isRetryEnabled = false,
                isSimulation = false,
                preflightCommitment = ConfirmationStatus.FINALIZED
            )
        } catch (error: Throwable) {
            Timber.i(error)
            throw StrigaWithdrawError.StrigaSendUsdcFailed("failed to send transaction", error)
        }

        Timber.i("Send success, tx = $txSignature")
        return Base58String(txSignature)
    }
}
