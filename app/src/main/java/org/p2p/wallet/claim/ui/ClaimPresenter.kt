package org.p2p.wallet.claim.ui

import android.content.res.Resources
import java.math.BigDecimal
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleMedium
import org.p2p.wallet.R
import org.p2p.wallet.claim.model.BridgeAmount
import org.p2p.wallet.claim.model.ClaimDetails
import org.p2p.wallet.common.mvp.BasePresenter

class ClaimPresenter(
    private val resources: Resources
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

    override fun loadData(tokenSymbol: String, tokenAmount: BigDecimal, fiatAmount: BigDecimal) {
        // TODO add real logic
        val fee: BigDecimal = BigDecimal(125.12)
        view?.apply {
            setTitle(resources.getString(R.string.bridge_claim_title_format, tokenSymbol))
            setTokenAmount("${tokenAmount.scaleMedium().formatToken()} $tokenSymbol")
            setFiatAmount(fiatAmount.asApproximateUsd(withBraces = false))
            showFee(
                if (fee == BigDecimal.ZERO) {
                    resources.getString(R.string.bridge_claim_fees_free)
                } else {
                    fee.asApproximateUsd(withBraces = false)
                }
            )
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
