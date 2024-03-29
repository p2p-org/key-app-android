package org.p2p.wallet.home.ui.wallet.handlers

import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.util.UUID
import org.p2p.core.utils.SOL_DECIMALS
import org.p2p.core.utils.emptyString
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionToken
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.home.events.AppLoaderFacade
import org.p2p.wallet.home.events.StrigaFeatureLoader
import org.p2p.wallet.home.ui.main.delegates.striga.offramp.StrigaOffRampCellModel
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.wallet.WalletContract
import org.p2p.wallet.home.ui.wallet.mapper.StrigaKycUiBannerMapper
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaBanner
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.smsinput.SmsInputTimer
import org.p2p.wallet.striga.offramp.withdraw.interactor.StrigaWithdrawInteractor
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaNoBankingDetailsProvided
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.transaction.model.HistoryTransactionStatus

class WalletStrigaHandler(
    private val strigaKycUiBannerMapper: StrigaKycUiBannerMapper,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val strigaOnRampInteractor: StrigaOnRampInteractor,
    private val strigaWithdrawInteractor: StrigaWithdrawInteractor,
    private val historyInteractor: HistoryInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val localFeatureFlags: InAppFeatureFlags,
    private val appLoaderFacade: AppLoaderFacade,
    private val strigaSmsInputTimer: SmsInputTimer,
) {
    // todo: this logic still hasn't described, but it helps to reuse otp timers
    //       and avoid getting new challengeId every button click
    private var onRampChallengeId: StrigaWithdrawalChallengeId? = null
    private var offRampChallengeId: StrigaWithdrawalChallengeId? = null

    suspend fun handleBannerClick(view: WalletContract.View?, item: StrigaBanner) {
        with(item.status) {
            val statusFromKycBanner = strigaKycUiBannerMapper.getKycStatusBannerFromTitle(bannerTitleResId)
            when {
                statusFromKycBanner == StrigaKycStatusBanner.PENDING -> {
                    view?.showKycPendingDialog()
                }
                statusFromKycBanner != null -> {
                    // hide banner if necessary
                    strigaUserInteractor.hideUserStatusBanner(statusFromKycBanner)

                    if (statusFromKycBanner == StrigaKycStatusBanner.VERIFICATION_DONE) {
                        view?.showStrigaBannerProgress(isLoading = true)
                        appLoaderFacade.cancel(StrigaFeatureLoader::class.java)
                        strigaWalletInteractor.loadDetailsForStrigaAccounts()
                            .onSuccess { view?.navigateToStrigaByBanner(statusFromKycBanner) }
                            .onFailure { view?.showUiKitSnackBar(messageResId = R.string.error_general_message) }
                        view?.showStrigaBannerProgress(isLoading = false)
                    } else {
                        view?.navigateToStrigaByBanner(statusFromKycBanner)
                    }
                }
                else -> {
                    view?.showAddMoneyDialog()
                }
            }
        }
    }

    suspend fun handleOnRampClick(view: WalletContract.View?, item: StrigaOnRampCellModel) {
        try {
            view?.showStrigaOnRampProgress(isLoading = true, tokenMint = item.tokenMintAddress)
            val challengeId = onRampChallengeId
                ?: strigaOnRampInteractor.onRampToken(item.amountAvailable, item.payload).unwrap()

            if (onRampChallengeId == null) {
                strigaSmsInputTimer.startSmsInputTimerFlow()
            }
            onRampChallengeId = challengeId

            view?.navigateToStrigaOnRampConfirmOtp(challengeId, item)
        } catch (e: Throwable) {
            Timber.e(e, "Error on claiming striga token")
            if (BuildConfig.DEBUG) {
                view?.showErrorMessage(IllegalStateException("Striga claiming is not supported yet", e))
            } else {
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        } finally {
            view?.showStrigaOnRampProgress(isLoading = false, tokenMint = item.tokenMintAddress)
        }
    }

    suspend fun handleOffRampClick(view: WalletContract.View?, item: StrigaOffRampCellModel) {
        try {
            view?.showStrigaOffRampProgress(isLoading = true, accountId = item.payload.accountId)

            if (localFeatureFlags.strigaSimulateIbanNotFilledFlag.featureValue) {
                throw StrigaNoBankingDetailsProvided()
            }

            val challengeId = offRampChallengeId
                ?: strigaWithdrawInteractor.withdrawEur(item.amountAvailable)

            // if there's no challenge id - start initial timer
            if (offRampChallengeId == null) {
                strigaSmsInputTimer.startSmsInputTimerFlow()
            }
            offRampChallengeId = challengeId

            view?.navigateToStrigaOffRampConfirmOtp(challengeId, item)
        } catch (e: StrigaNoBankingDetailsProvided) {
            Timber.i("No banking details provided. Navigate to withdrawal screen to fill it")
            view?.navigateToOffRampWithdrawEur(item.amountAvailable)
        } catch (e: Throwable) {
            Timber.e(e, "Error on confirming striga withdrawal")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        } finally {
            view?.showStrigaOffRampProgress(isLoading = false, accountId = item.payload.accountId)
        }
    }

    /**
     * If user confirmed claim using OTP - we add claim transaction as pending
     */
    suspend fun handleOnRampConfirmed(claimedToken: StrigaOnRampCellModel) {
        onRampChallengeId = null
        kotlin.runCatching {
            addOnRampPendingTransaction(claimedToken)
        }.onFailure { Timber.e(it, "Failed to add pending transaction on onramp") }
    }

    fun handleOffRampConfirmed(token: StrigaOffRampCellModel) {
        offRampChallengeId = null
    }

    private suspend fun addOnRampPendingTransaction(onRampTokenItem: StrigaOnRampCellModel) {
        val strigaUserCryptoAddress = strigaWalletInteractor.getCryptoAccountDetails().depositAddress

        historyInteractor.addPendingTransaction(
            txSignature = emptyString(),
            mintAddress = onRampTokenItem.tokenMintAddress,
            transaction = createReceiveTransaction(onRampTokenItem, strigaUserCryptoAddress)
        )
    }

    private fun createReceiveTransaction(
        claimedToken: StrigaOnRampCellModel,
        strigaUserCryptoAddress: String
    ): RpcHistoryTransaction.Transfer {
        return RpcHistoryTransaction.Transfer(
            // no signature available, so randomize it
            signature = UUID.randomUUID().toString(),
            date = ZonedDateTime.now(),
            blockNumber = RpcHistoryTransaction.UNDEFINED_BLOCK_NUMBER,
            status = HistoryTransactionStatus.PENDING,
            type = RpcHistoryTransactionType.RECEIVE,
            senderAddress = strigaUserCryptoAddress,
            token = RpcHistoryTransactionToken(
                symbol = claimedToken.tokenSymbol,
                decimals = SOL_DECIMALS,
                logoUrl = claimedToken.tokenIcon
            ),
            amount = claimedToken.amountAvailable.let { RpcHistoryAmount(it, it) },
            destination = tokenKeyProvider.publicKey,
            counterPartyUsername = null,
            fees = null
        )
    }
}
