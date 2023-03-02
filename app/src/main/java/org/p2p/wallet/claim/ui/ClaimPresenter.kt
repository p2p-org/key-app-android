package org.p2p.wallet.claim.ui

import android.content.res.Resources
import java.math.BigDecimal
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleMedium
import org.p2p.wallet.R
import org.p2p.wallet.claim.model.ClaimDetails
import org.p2p.wallet.claim.model.ClaimFee
import org.p2p.wallet.common.mvp.BasePresenter

class ClaimPresenter(
    private val resources: Resources
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

    override fun loadData(tokenSymbol: String, tokenAmount: BigDecimal, fiatAmount: BigDecimal) {
        // TODO add real logic
        val fee: BigDecimal = BigDecimal(125.12)
        view?.apply {
            setTitle(resources.getString(R.string.claiming_title_format, tokenSymbol))
            setTokenAmount("${tokenAmount.scaleMedium().formatToken()} $tokenSymbol")
            setFiatAmount(fiatAmount.asApproximateUsd(withBraces = false))
            showFee(
                if (fee == BigDecimal.ZERO) {
                    resources.getString(R.string.claiming_fees_free)
                } else {
                    fee.asApproximateUsd(withBraces = false)
                }
            )
        }
    }

    override fun onFeeClicked() {
        // TODO connect real fee details
        val tokenSymbol = "WETH"
        val claimDetails: ClaimDetails = ClaimDetails(
            willGet = ClaimFee(
                tokenSymbol,
                BigDecimal(0.999717252),
                BigDecimal(1215.75)
            ),
            networkFee = ClaimFee.free(),
            accountCreationFee = ClaimFee(
                tokenSymbol,
                BigDecimal(0.003),
                BigDecimal(0.01)
            ),
            bridgeFee = ClaimFee(
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
