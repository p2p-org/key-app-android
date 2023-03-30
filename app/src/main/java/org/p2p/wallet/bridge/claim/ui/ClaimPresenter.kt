package org.p2p.wallet.bridge.claim.ui

import android.content.res.Resources
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.isConnectionError
import org.p2p.core.utils.toLamports
import org.p2p.core.wrapper.HexString
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.bridge.model.BridgeResult.Error.ContractError
import org.p2p.wallet.bridge.model.BridgeResult.Error.NotEnoughAmount
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getErrorMessage

const val DEFAULT_DELAY_IN_MILLIS = 30_000L

class ClaimPresenter(
    private val tokenToClaim: Token.Eth,
    private val claimInteractor: ClaimInteractor,
    private val userLocalRepository: UserLocalRepository,
    private val ethereumRepository: EthereumRepository,
    private val transactionManager: TransactionManager,
    private val claimUiMapper: ClaimUiMapper,
    private val resources: Resources,
    private val appScope: AppScope,
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

    private var refreshJob: Job? = null
    private var claimDetails: ClaimDetails? = null
    private var latestTransactions: List<HexString> = emptyList()
    private var latestBundleId: String = emptyString()
    private var refreshJobDelayTimeInMillis = DEFAULT_DELAY_IN_MILLIS
    private var eth: Token.Eth? = null

    override fun attach(view: ClaimContract.View) {
        super.attach(view)
        launch {
            if (eth == null) {
                eth = ethereumRepository.getUserEthToken()
            }
        }
        startRefreshJob()
        view.apply {
            claimUiMapper.mapScreenData(tokenToClaim).apply {
                setTitle(title)
                setTokenIconUrl(tokenIconUrl)
                setTokenAmount(tokenFormattedAmount)
                setFiatAmount(fiatFormattedAmount)
            }
        }
    }

    private fun startRefreshJob(delayMillis: Long = 0) {
        refreshJob?.cancel()
        refreshJob = launch {
            delay(delayMillis)
            latestTransactions = emptyList()
            claimDetails = null
            view?.setClaimButtonState(isButtonEnabled = false)
            view?.showFee(claimUiMapper.getTextSkeleton())
            val totalToClaim = tokenToClaim.total.toLamports(tokenToClaim.decimals)
            try {
                claimInteractor.getEthereumBundle(
                    erc20Token = tokenToClaim.getEthAddress().takeIf { !tokenToClaim.isEth },
                    amount = totalToClaim.toString()
                ).apply {
                    latestBundleId = bundleId
                    refreshJobDelayTimeInMillis = getExpirationDateInMillis() - ZonedDateTime.now().dateMilli()
                    latestTransactions = transactions
                    showFees(resultAmount, fees, compensationDeclineReason.isEmpty())
                    val finalValue = claimUiMapper.makeResultAmount(resultAmount, tokenToClaim)
                    view?.showClaimButtonValue(finalValue.formattedTokenAmount.orEmpty())
                }
            } catch (error: Throwable) {
                val messageResId = when {
                    error is NotEnoughAmount || error is ContractError -> R.string.bridge_claim_fees_bigger_error
                    error.isConnectionError() -> R.string.common_offline_error
                    else -> null
                }
                if (messageResId != null) view?.showUiKitSnackBar(messageResId = messageResId)
                Timber.e(error, "Error on getting bundle for claim")
                view?.showFee(TextViewCellModel.Raw(TextContainer(R.string.bridge_claim_fees_unavailable)))
                view?.setClaimButtonState(isButtonEnabled = false)
            } finally {
                startRefreshJob(refreshJobDelayTimeInMillis)
            }
        }
    }

    private fun showFees(resultAmount: BridgeFee, fees: BridgeBundleFees, isFree: Boolean) {
        view?.showFee(claimUiMapper.mapFeeTextContainer(fees, isFree))
        claimDetails = claimUiMapper.makeClaimDetails(tokenToClaim, resultAmount, fees.takeUnless { isFree }, eth)
        view?.setClaimButtonState(isButtonEnabled = true)
    }

    override fun onFeeClicked() {
        view?.showClaimFeeInfo(claimDetails ?: return)
    }

    override fun onSendButtonClicked() {
        appScope.launch {
            try {
                val signedTransactions = latestTransactions.map { ethereumRepository.signTransaction(it) }
                val progressDetails = claimUiMapper.prepareShowProgress(tokenToClaim, claimDetails)
                view?.showProgressDialog(latestBundleId, progressDetails)

                claimInteractor.sendEthereumBundle(signedTransactions)
                val transactionState = TransactionState.ClaimSuccess(latestBundleId, tokenToClaim.tokenSymbol)
                transactionManager.emitTransactionState(latestBundleId, transactionState)
            } catch (e: BridgeResult.Error) {
                val message = e.getErrorMessage { res -> resources.getString(res) }
                transactionManager.emitTransactionState(latestBundleId, TransactionState.Error(message))
                Timber.e(e, "Failed to send signed bundle")
            }
        }
    }

    override fun detach() {
        refreshJob?.cancel()
        super.detach()
    }
}
