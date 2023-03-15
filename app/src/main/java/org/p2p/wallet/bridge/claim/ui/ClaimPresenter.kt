package org.p2p.wallet.bridge.claim.ui

import android.content.res.Resources
import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleMedium
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.common.mvp.BasePresenter

class ClaimPresenter(
    private val tokenToClaim: Token.Eth,
    private val resources: Resources
) : BasePresenter<ClaimContract.View>(), ClaimContract.Presenter {

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
