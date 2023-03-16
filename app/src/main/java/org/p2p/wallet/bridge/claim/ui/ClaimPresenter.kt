package org.p2p.wallet.bridge.claim.ui

import android.content.res.Resources
import android.view.Gravity
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.model.TextHighlighting
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.DEFAULT_DECIMAL
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isConnectionError
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.model.BridgeBundleFee
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toPx

const val DEFAULT_DELAY_IN_MILLIS = 30_000L

class ClaimPresenter(
    private val tokenToClaim: Token.Eth,
    private val claimInteractor: ClaimInteractor,
    private val userInteractor: UserInteractor,
    private val ethereumRepository: EthereumRepository,
    private val resources: Resources,
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

    private var refreshJob: Job? = null
    private var claimDetails: ClaimDetails? = null
    private var latestBundle: BridgeBundle? = null
    private var sol: Token.Active? = null

    override fun attach(view: ClaimContract.View) {
        super.attach(view)
        launch {
            sol = userInteractor.getUserSolToken()
        }
        startRefreshJob()
        view.apply {
            setTitle(resources.getString(R.string.bridge_claim_title_format, tokenToClaim.tokenSymbol))
            setTokenIconUrl(tokenToClaim.iconUrl)
            setTokenAmount("${tokenToClaim.total.scaleMedium().formatToken()} ${tokenToClaim.tokenSymbol}")
            setFiatAmount(tokenToClaim.totalInUsd.orZero().asApproximateUsd(withBraces = false))
        }
    }

    private fun startRefreshJob(delayMillis: Long? = null) {
        refreshJob?.cancel()
        refreshJob = launch {
            if (delayMillis != null) delay(delayMillis)
            latestBundle = null
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
            val total = tokenToClaim.total
            val totalToClaim = total.toLamports(tokenToClaim.decimals)
            try {
                val bundle = claimInteractor.getEthereumBundle(
                    tokenToClaim.getEthAddress().takeIf { !tokenToClaim.isEth },
                    totalToClaim.toString()
                )
                val timeExpires = bundle.expiresAt.seconds.inWholeMilliseconds.toZonedDateTime().dateMilli()
                val timerToSet = timeExpires - ZonedDateTime.now().dateMilli()
                parseFees(bundle.fees)
                latestBundle = bundle
                startRefreshJob(timerToSet)
            } catch (error: Throwable) {
                if (error.isConnectionError()) {
                    view?.showUiKitSnackBar(messageResId = R.string.common_offline_error)
                }
                Timber.e(error, "Error on getting bundle for claim")
                view?.showFee(TextViewCellModel.Raw(TextContainer(R.string.bridge_claim_fees_unavailable)))
                view?.setClaimButtonState(isButtonEnabled = false)
                latestBundle = null
                claimDetails = null
                startRefreshJob(DEFAULT_DELAY_IN_MILLIS)
            }
        }
    }

    private fun parseFees(fees: BridgeBundleFees) {
        val tokenSymbol = tokenToClaim.tokenSymbol
        val decimals = tokenToClaim.decimals
        val feeList = listOf(fees.arbiterFee, fees.gasEth, fees.createAccount)
        val fee: BigDecimal = feeList.sumOf { it?.amountInUsd?.toBigDecimal() ?: BigDecimal.ZERO }
        val feeValue = if (fee == BigDecimal.ZERO) {
            resources.getString(R.string.bridge_claim_fees_free)
        } else {
            fee.asApproximateUsd(withBraces = false)
        }
        view?.showFee(TextViewCellModel.Raw(TextContainer(feeValue)))
        claimDetails = ClaimDetails(
            willGetAmount = BridgeAmount(
                tokenSymbol,
                tokenToClaim.total,
                tokenToClaim.totalInUsd
            ),
            networkFee = fees.gasEth.toBridgeAmount(tokenSymbol, decimals),
            accountCreationFee = fees.createAccount.toBridgeAmount(
                tokenSymbol = sol?.tokenSymbol ?: Constants.SOL_SYMBOL,
                decimals = sol?.decimals ?: DEFAULT_DECIMAL
            ),
            bridgeFee = fees.arbiterFee.toBridgeAmount(tokenSymbol, decimals)
        )
        val totalFees = feeList.sumOf { it?.amountInToken(decimals) ?: BigDecimal.ZERO }
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
        claimDetails?.let {
            view?.showClaimFeeInfo(it)
        }
    }

    override fun onSendButtonClicked() {
        val lastBundle = latestBundle ?: return
        launch {
            try {
                val signedTransactions = lastBundle.transactions.map { ethereumRepository.signTransaction(it) }
                lastBundle.signatures = signedTransactions.map { it.first }
                claimInteractor.sendEthereumBundle(lastBundle)
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
                    amountUsd = amountUsd.asNegativeUsdTransaction(),
                    recipient = null,
                    totalFees = feeList.mapNotNull { it.toTextHighlighting() }
                )
                view?.showProgressDialog(lastBundle.bundleId, progressDetails)
            } catch (e: BridgeResult.Error) {
                Timber.e(e)
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
