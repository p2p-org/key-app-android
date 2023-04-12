package org.p2p.wallet.bridge.claim.ui

import android.content.res.Resources
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.isConnectionError
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.core.wrapper.HexString
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.anatytics.ClaimAnalytics
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.bridge.model.BridgeResult.Error.ContractError
import org.p2p.wallet.bridge.model.BridgeResult.Error.NotEnoughAmount
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.ui.main.UserTokensPolling
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getErrorMessage

const val DEFAULT_DELAY_IN_MILLIS = 30_000L

class ClaimPresenter(
    private val tokenToClaim: Token.Eth,
    private val ethereumInteractor: EthereumInteractor,
    private val transactionManager: TransactionManager,
    private val claimUiMapper: ClaimUiMapper,
    private val resources: Resources,
    private val appScope: AppScope,
    private val userTokensPolling: UserTokensPolling,
    private val claimAnalytics: ClaimAnalytics
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

    private var refreshJob: Job? = null
    private var claimDetails: ClaimDetails? = null
    private var latestTransactions: List<HexString> = emptyList()
    private var latestBundleId: String = emptyString()
    private var latestBundle: BridgeBundle? = null
    private var minAmountForFreeFee: BigDecimal = BigDecimal.ZERO
    private var refreshJobDelayTimeInMillis = DEFAULT_DELAY_IN_MILLIS

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
                val newBundle = fetchBundle()
                minAmountForFreeFee = ethereumInteractor.getClaimMinAmountForFreeFee()
                showFees(
                    resultAmount = newBundle.resultAmount,
                    fees = newBundle.fees,
                    isFree = newBundle.compensationDeclineReason.isEmpty(),
                    minAmountForFreeFee = minAmountForFreeFee
                )
                val finalValue = claimUiMapper.makeResultAmount(newBundle.resultAmount)
                view?.showClaimButtonValue(finalValue.formattedTokenAmount.orEmpty())
            } catch (error: Throwable) {
                val messageResId = when {
                    error is NotEnoughAmount || error is ContractError -> R.string.bridge_claim_fees_bigger_error
                    error.isConnectionError() -> R.string.common_offline_error
                    else -> null
                }
                if (messageResId != null) {
                    view?.showUiKitSnackBar(messageResId = messageResId)
                }
                Timber.e(error, "Error on getting bundle for claim")

                view?.showFee(TextViewCellModel.Raw(TextContainer(R.string.bridge_claim_fees_unavailable)))
                view?.setClaimButtonState(isButtonEnabled = false)
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
        claimAnalytics.logFeesButtonClicked()
        view?.showFee(claimUiMapper.mapFeeTextContainer(fees, isFree))

        claimDetails = claimUiMapper.makeClaimDetails(
            tokenToClaim = tokenToClaim,
            resultAmount = resultAmount,
            fees = fees.takeUnless { isFree },
            minAmountForFreeFee = minAmountForFreeFee
        )
        view?.setClaimButtonState(isButtonEnabled = true)
    }

    override fun onFeeClicked() {
        view?.showClaimFeeInfo(claimDetails ?: return)
    }

    override fun onSendButtonClicked() {
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
                    tokenToClaim = tokenToClaim,
                    claimDetails = claimDetails
                )
                view?.showProgressDialog(
                    bundleId = latestBundleId,
                    data = progressDetails
                )

                ethereumInteractor.sendClaimBundle(signatures = signatures)

                val transactionState = TransactionState.ClaimSuccess(
                    bundleId = latestBundleId,
                    sourceTokenSymbol = tokenToClaim.tokenSymbol
                )
                transactionManager.emitTransactionState(
                    transactionId = latestBundleId,
                    state = transactionState
                )
            } catch (e: BridgeResult.Error) {
                val message = e.getErrorMessage { res -> resources.getString(res) }
                Timber.e(e, "Failed to send signed bundle")
            }
        }
    }

    private fun reset() {
        latestTransactions = emptyList()
        claimDetails = null
        view?.setClaimButtonState(isButtonEnabled = false)
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
            refreshJobDelayTimeInMillis = newBundle.getExpirationDateInMillis() - ZonedDateTime.now().dateMilli()
            latestTransactions = newBundle.transactions
        }
    }

    private fun setupView() {
        val screenData = claimUiMapper.mapScreenData(tokenToClaim)
        val view = view ?: return
        with(view) {
            setTitle(screenData.title)
            setTokenIconUrl(screenData.tokenIconUrl)
            setTokenAmount(screenData.tokenFormattedAmount)
            setFiatAmount(screenData.fiatFormattedAmount)
        }
    }

    override fun detach() {
        refreshJob?.cancel()
        super.detach()
    }
}
