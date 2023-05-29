package org.p2p.wallet.moonpay.ui

import androidx.fragment.app.Fragment
import org.p2p.core.token.Token
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment

class BuyFragmentFactory(
    private val buyFeatureToggle: NewBuyFeatureToggle
) {

    fun buyFragment(tokenForBuy: Token): Fragment {
        return if (buyFeatureToggle.isFeatureEnabled) {
            NewBuyFragment.create(tokenForBuy)
        } else {
            BuySolanaFragment.create(tokenForBuy)
        }
    }
}
