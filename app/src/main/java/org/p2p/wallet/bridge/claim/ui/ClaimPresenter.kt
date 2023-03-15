package org.p2p.wallet.bridge.claim.ui

import android.content.res.Resources
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.toLamports
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.common.mvp.BasePresenter

const val DEFAULT_DELAY_IN_MILLIS = 30L

class ClaimPresenter(
    private val tokenToClaim: Token.Eth,
    private val claimInteractor: ClaimInteractor,
    private val resources: Resources
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

    private var refreshJob: Job? = null

    override fun attach(view: ClaimContract.View) {
        super.attach(view)
        val fee: BigDecimal = BigDecimal.ZERO
        view.apply {
            setTitle(resources.getString(R.string.bridge_claim_title_format, tokenToClaim.tokenSymbol))
            setTokenIconUrl(tokenToClaim.iconUrl)
            setTokenAmount("${tokenToClaim.total.scaleMedium().formatToken()} ${tokenToClaim.tokenSymbol}")
            setFiatAmount(tokenToClaim.totalInUsd.orZero().asApproximateUsd(withBraces = false))
            showFee(
                if (fee == BigDecimal.ZERO) {
                    resources.getString(R.string.bridge_claim_fees_free)
                } else {
                    fee.asApproximateUsd(withBraces = false)
                }
            )
        }
    }

    private fun startRefreshJob(delayMillis: Long?) {
        refreshJob = launch {
            if (delayMillis != null) delay(delayMillis)
            val total = tokenToClaim.total // BigDecimal(1)
            val totalToClaim = total.toLamports(tokenToClaim.decimals)
            try {
                val bundle = claimInteractor.getEthereumBundle(
                    tokenToClaim.publicKey,
                    totalToClaim.toString()
                )
                val timeExpires = bundle.expiresAt.toZonedDateTime().dateMilli()
                val timerToSet = timeExpires - System.currentTimeMillis()
                startRefreshJob(timerToSet)
            } catch (error: Throwable) {
                view?.showErrorMessage(error)
                Timber.d(error, "Error on getting bundle for claim")
                // TODO check cases and restart job if needed! startRefreshJob(DEFAULT_DELAY_IN_MILLIS)
            }
        }
    }

    override fun onFeeClicked() {
        // TODO connect real fee details
        val tokenSymbol = "WETH"
        val claimDetails = ClaimDetails(
            willGetAmount = BridgeAmount(
                tokenSymbol,
                BigDecimal(0.999717252),
                BigDecimal(1215.75)
            ),
            networkFee = BridgeAmount.zero(),
            accountCreationFee = BridgeAmount(
                tokenSymbol,
                BigDecimal(0.003),
                BigDecimal(0.01)
            ),
            bridgeFee = BridgeAmount(
                tokenSymbol,
                BigDecimal(0.01),
                BigDecimal(4.12)
            )
        )
        view?.showClaimFeeInfo(claimDetails)
    }

    override fun onSendButtonClicked() {
        // TODO implement claim logic
    }
}
