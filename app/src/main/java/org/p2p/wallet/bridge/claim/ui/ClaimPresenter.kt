package org.p2p.wallet.bridge.claim.ui

import android.content.res.Resources
import android.view.Gravity
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.model.TextHighlighting
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.asPositiveUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isConnectionError
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.wrapper.HexString
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeBundleFee
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.toPx

const val DEFAULT_DELAY_IN_MILLIS = 30_000L

class ClaimPresenter(
    private val tokenToClaim: Token.Eth,
    private val claimInteractor: ClaimInteractor,
    private val userLocalRepository: UserLocalRepository,
    private val ethereumRepository: EthereumRepository,
    private val transactionManager: TransactionManager,
    private val resources: Resources,
    private val appScope: AppScope,
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

    private var refreshJob: Job? = null
    private var claimDetails: ClaimDetails? = null
    private var latestTransactions: List<HexString> = emptyList()
    private var latestBundleId: String = emptyString()
    private var refreshJobDelayTimeInMillis = DEFAULT_DELAY_IN_MILLIS
    private var eth: TokenData? = null

    override fun attach(view: ClaimContract.View) {
        super.attach(view)
        launch {
            eth = userLocalRepository.findTokenData(Constants.WRAPPED_ETH_MINT)
        }
        startRefreshJob()
        view.apply {
            setTitle(resources.getString(R.string.bridge_claim_title_format, tokenToClaim.tokenSymbol))
            setTokenIconUrl(tokenToClaim.iconUrl)
            setTokenAmount("${tokenToClaim.total.scaleMedium().formatToken()} ${tokenToClaim.tokenSymbol}")
            setFiatAmount(tokenToClaim.totalInUsd.orZero().asApproximateUsd(withBraces = false))
        }
    }

    private fun startRefreshJob(delayMillis: Long = 0) {
        refreshJob?.cancel()
        refreshJob = launch {
            delay(delayMillis)
            latestTransactions = emptyList()
            claimDetails = null
            view?.setClaimButtonState(isButtonEnabled = false)
            view?.showFee(
                TextViewCellModel.Skeleton(
                    SkeletonCellModel(
                        height = 24.toPx(),
                        width = 100.toPx(),
                        radius = 4f.toPx(),
                        gravity = Gravity.END
                    )
                )
            )
            val totalToClaim = tokenToClaim.total.toLamports(tokenToClaim.decimals)
            try {
                claimInteractor.getEthereumBundle(
                    erc20Token = tokenToClaim.getEthAddress().takeIf { !tokenToClaim.isEth },
                    amount = totalToClaim.toString()
                ).apply {
                    latestBundleId = bundleId
                    refreshJobDelayTimeInMillis = getExpirationDateInMillis() - ZonedDateTime.now().dateMilli()
                    latestTransactions = transactions
                    parseFees(fees, compensationDeclineReason.isEmpty())
                }
            } catch (error: Throwable) {
                if (error.isConnectionError()) {
                    view?.showUiKitSnackBar(messageResId = R.string.common_offline_error)
                }
                Timber.e(error, "Error on getting bundle for claim")
                view?.showFee(TextViewCellModel.Raw(TextContainer(R.string.bridge_claim_fees_unavailable)))
                view?.setClaimButtonState(isButtonEnabled = false)
            } finally {
                startRefreshJob(refreshJobDelayTimeInMillis)
            }
        }
    }

    private fun parseFees(fees: BridgeBundleFees, isFree: Boolean) {
        val tokenSymbol = tokenToClaim.tokenSymbol
        val decimals = tokenToClaim.decimals
        val feeList = listOf(fees.arbiterFee, fees.gasEth, fees.createAccount)
        val fee: BigDecimal = feeList.sumOf { it.amountInUsd?.toBigDecimal() ?: BigDecimal.ZERO }
        val feeValue = if (isFree) {
            resources.getString(R.string.bridge_claim_fees_free)
        } else {
            fee.asApproximateUsd(withBraces = false)
        }
        view?.showFee(TextViewCellModel.Raw(TextContainer(feeValue)))
        val totalFees: BigDecimal
        if (isFree) {
            totalFees = BigDecimal.ZERO
        } else {
            totalFees = feeList.sumOf { it.amountInToken(decimals) }
            claimDetails = ClaimDetails(
                willGetAmount = BridgeAmount(
                    tokenSymbol,
                    tokenToClaim.total,
                    tokenToClaim.totalInUsd
                ),
                networkFee = eth?.let { ethTokenData ->
                    fees.gasEth.toBridgeAmount(
                        tokenSymbol = ethTokenData.symbol,
                        decimals = ethTokenData.decimals
                    )
                } ?: fees.gasEth.toBridgeAmount(tokenSymbol, decimals),
                accountCreationFee = fees.createAccount.toBridgeAmount(tokenSymbol, decimals),
                bridgeFee = fees.arbiterFee.toBridgeAmount(tokenSymbol, decimals)
            )
        }

        val finalValue = tokenToClaim.total - totalFees
        view?.showClaimButtonValue("${finalValue.scaleMedium().formatToken()} ${tokenToClaim.tokenSymbol}")
        view?.setClaimButtonState(isButtonEnabled = true)
    }

    private fun BridgeBundleFee?.toBridgeAmount(
        tokenSymbol: String,
        decimals: Int,
    ): BridgeAmount {
        return BridgeAmount(
            tokenSymbol = tokenSymbol,
            tokenAmount = this?.amountInToken(decimals).takeIf { !it.isNullOrZero() },
            fiatAmount = this?.amountInUsd?.toBigDecimalOrZero()
        )
    }

    override fun onFeeClicked() {
        view?.showClaimFeeInfo(claimDetails ?: return)
    }

    override fun onSendButtonClicked() {
        appScope.launch {
            try {
                val signedTransactions = latestTransactions.map { ethereumRepository.signTransaction(it) }
                val transactionDate = Date()
                val amountTokens = "${tokenToClaim.total.scaleMedium().formatToken()} ${tokenToClaim.tokenSymbol}"
                val amountUsd = tokenToClaim.totalInUsd.orZero()
                val feeList = listOfNotNull(
                    claimDetails?.networkFee,
                    claimDetails?.accountCreationFee,
                    claimDetails?.bridgeFee
                )
                val progressDetails = NewShowProgress(
                    date = transactionDate,
                    tokenUrl = tokenToClaim.iconUrl.orEmpty(),
                    amountTokens = amountTokens,
                    amountUsd = amountUsd.asPositiveUsdTransaction(),
                    recipient = null,
                    totalFees = feeList.mapNotNull { it.toTextHighlighting() }
                )
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

    private fun BridgeAmount.toTextHighlighting(): TextHighlighting? {
        if (isFree) return null
        val usdText = formattedFiatAmount.orEmpty()
        val commonText = "$formattedTokenAmount $usdText"
        return TextHighlighting(
            commonText = commonText,
            highlightedText = usdText
        )
    }

    override fun detach() {
        refreshJob?.cancel()
        super.detach()
    }
}
