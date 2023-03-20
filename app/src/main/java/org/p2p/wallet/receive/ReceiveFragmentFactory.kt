package org.p2p.wallet.receive

import androidx.fragment.app.Fragment
import org.p2p.core.token.Token
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.receive.tokenselect.ReceiveTokensFragment

class ReceiveFragmentFactory(private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle) {

    fun receiveFragment(token: Token.Active? = null): Fragment {
        return if (ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            ReceiveTokensFragment.create()
        } else {
            ReceiveSolanaFragment.create(token = token)
        }
    }
}
