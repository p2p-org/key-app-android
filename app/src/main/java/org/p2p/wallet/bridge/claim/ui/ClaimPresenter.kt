package org.p2p.wallet.bridge.claim.ui

import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.isConnectionError
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.core.wrapper.HexString
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.bridge.analytics.ClaimAnalytics
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.bridge.model.BridgeResult.Error.ContractError
import org.p2p.wallet.bridge.model.BridgeResult.Error.NotEnoughAmount
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.ui.main.UserTokensPolling
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.utils.emptyString

const val DEFAULT_DELAY_IN_MILLIS = 30_000L

class ClaimPresenter(
    private val tokenToClaim: Token.Eth,
    private val ethereumInteractor: EthereumInteractor,
    private val transactionManager: TransactionManager,
    private val claimUiMapper: ClaimUiMapper,
    private val appScope: AppScope,
    private val claimAnalytics: ClaimAnalytics,
    private val userTokensPolling: UserTokensPolling,
    private val alarmErrorsLogger: AlarmErrorsLogger
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

    private var refreshJob: Job? = null
    private var claimDetails: ClaimDetails? = null
    private var latestTransactions: List<HexString> = emptyList()
    private var latestBundleId: String = emptyString()
    private var latestBundle: BridgeBundle? = null
    private var minAmountForFreeFee: BigDecimal = BigDecimal.ZERO
    private var refreshJobDelayTimeInMillis = DEFAULT_DELAY_IN_MILLIS
    private var isLastErrorWasNotEnoughFundsError = false

    override fun attach(view: ClaimContract.View) {
        super.attach(view)
        claimAnalytics.logScreenOpened(ClaimAnalytics.ClaimOpenedFrom.MAIN)
        startRefreshJob()
        setupView()
    }

    private fun startRefreshJob(delayMillis: Long = 0) {
        refreshJob?.cancel()
        refreshJob = launchSupervisor {
            delay(delayMillis)
            reset()
            try {
                minAmountForFreeFee = ethereumInteractor.getClaimMinAmountForFreeFee()
                view?.setMinAmountForFreeFee(minAmountForFreeFee)
                val newBundle = fetchBundle()
                showFees(
                    resultAmount = newBundle.resultAmount,
                    fees = newBundle.fees,
                    isFree = newBundle.compensationDeclineReason.isEmpty(),
                    minAmountForFreeFee = minAmountForFreeFee
                )
                val finalValue = claimUiMapper.makeResultAmount(newBundle.resultAmount)
                val amountInFiat = finalValue.fiatAmount?.asUsd()
                if (amountInFiat == null) {
                    view?.setWillGetVisibility(isVisible = false)
                } else {
                    view?.showWillGet(TextViewCellModel.Raw(TextContainer(finalValue.formattedTokenAmount.orEmpty())))
                    view?.setWillGetVisibility(isVisible = true)
                }
            } catch (error: Throwable) {
                Timber.e(error, "Error on getting bundle for claim")
                val isNotEnoughFundsError = error is NotEnoughAmount || error is ContractError
                val messageResId = when {
                    isNotEnoughFundsError -> R.string.bridge_claim_fees_bigger_error
                    error.isConnectionError() -> R.string.common_offline_error
                    error is BridgeResult.Error -> R.string.bridge_claim_fees_common_error
                    else -> null
                }
                if (messageResId != null) {
                    view?.showUiKitSnackBar(messageResId = messageResId)
                }

                val feeErrorRes = if (isNotEnoughFundsError) {
                    view?.setButtonText(
                        TextViewCellModel.Raw(
                            TextContainer(R.string.bridge_claim_bottom_button_add_funds)
                        )
                    )
                    R.string.bridge_claim_fees_more_then
                } else {
                    R.string.bridge_claim_fees_unavailable
                }
                view?.showFee(TextViewCellModel.Raw(TextContainer(feeErrorRes)))
                view?.setClaimButtonState(isButtonEnabled = isNotEnoughFundsError)
                view?.setFeeInfoVisibility(isVisible = false)
                view?.setWillGetVisibility(isVisible = false)
                isLastErrorWasNotEnoughFundsError = isNotEnoughFundsError
            } finally {
                startRefreshJob(refreshJobDelayTimeInMillis)
            }
        }
    }

    private fun showFees(
        resultAmount: BridgeFee,
        fees: BridgeBundleFees,
        isFree: Boolean,
        minAmountForFreeFee: BigDecimal
    ) {
        view?.showFee(claimUiMapper.mapFeeTextContainer(fees, isFree))

        claimDetails = claimUiMapper.makeClaimDetails(
            isFree = isFree,
            resultAmount = resultAmount,
            fees = fees,
            minAmountForFreeFee = minAmountForFreeFee,
            transactionDate = ZonedDateTime.now()
        )
        view?.setClaimButtonState(isButtonEnabled = true)
    }

    override fun onFeeClicked() {
        view?.showClaimFeeInfo(claimDetails ?: return)
        claimAnalytics.logFeesButtonClicked()
    }

    override fun onSendButtonClicked() {
        if (isLastErrorWasNotEnoughFundsError) {
            view?.openReceive()
        } else {
            sendBundle()
        }
    }

    private fun sendBundle() {
        latestBundle?.apply {
            with(resultAmount) {
                claimAnalytics.logConfirmClaimButtonClicked(
                    tokenSymbol = symbol,
                    tokenAmount = amountInToken,
                    tokenAmountInFiat = amountInUsd?.toBigDecimal().orZero(),
                    isFree = compensationDeclineReason.isEmpty()
                )
            }
        }

        appScope.launch {
            try {
                val signatures = latestTransactions.map { unsignedTransaction ->
                    ethereumInteractor.signClaimTransaction(transaction = unsignedTransaction)
                }
                val progressDetails = claimUiMapper.prepareShowProgress(
                    amountToClaim = tokenToClaim.total,
                    iconUrl = tokenToClaim.iconUrl.orEmpty(),
                    claimDetails = claimDetails
                )
                view?.showProgressDialog(
                    bundleId = latestBundleId,
                    data = progressDetails
                )

                ethereumInteractor.sendClaimBundle(signatures = signatures)

                val transactionState = TransactionState.ClaimProgress(bundleId = latestBundleId)
                transactionManager.emitTransactionState(
                    transactionId = latestBundleId,
                    state = transactionState
                )
                userTokensPolling.refreshTokens()
            } catch (e: BridgeResult.Error) {
                Timber.e(e, "Failed to send signed bundle: ${e.message}")
                logClaimErrorAlarm(e)
            }
        }
    }

    private fun reset() {
        latestTransactions = emptyList()
        claimDetails = null
        isLastErrorWasNotEnoughFundsError = false
        view?.setClaimButtonState(isButtonEnabled = false)
        view?.setFeeInfoVisibility(isVisible = true)
        view?.setWillGetVisibility(isVisible = false)
        view?.showFee(claimUiMapper.getTextSkeleton())
    }

    private suspend fun fetchBundle(): BridgeBundle {
        val totalToClaim = tokenToClaim.total.toLamports(tokenToClaim.decimals)

        return ethereumInteractor.getEthereumBundle(
            erc20Token = tokenToClaim.getEthAddress().takeIf { !tokenToClaim.isEth },
            amount = totalToClaim.toString()
        ).also { newBundle ->
            latestBundle = newBundle
            latestBundleId = newBundle.bundleId
            refreshJobDelayTimeInMillis = newBundle.getExpirationDateInMillis() - ZonedDateTime.now().toEpochSecond()
            latestTransactions = newBundle.transactions
        }
    }

    private fun setupView() {
        val screenData = claimUiMapper.mapScreenData(tokenToClaim)
        val view = view ?: return
        with(view) {
            setTokenIconUrl(screenData.tokenIconUrl)
            setTokenAmount(screenData.tokenFormattedAmount)
            setFiatAmount(screenData.fiatFormattedAmount)
        }
    }

    override fun detach() {
        refreshJob?.cancel()
        super.detach()
    }

    private fun logClaimErrorAlarm(error: Throwable) {
        val claimAmount = latestBundle?.resultAmount?.amountInToken?.toPlainString() ?: "0"
        alarmErrorsLogger.triggerBridgeClaimAlarm(
            tokenToClaim = tokenToClaim,
            claimAmount = claimAmount,
            error = error
        )
    }
}
