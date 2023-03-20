package org.p2p.wallet.swap.ui

import androidx.fragment.app.Fragment
import org.p2p.core.token.Token
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewSwapEnabledFeatureToggle
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.swap.ui.orca.SwapOpenedFrom

class SwapFragmentFactory(private val newSwapEnabledFeatureToggle: NewSwapEnabledFeatureToggle) {

    fun swapFragment(token: Token.Active? = null, source: SwapOpenedFrom): Fragment {
        return if (newSwapEnabledFeatureToggle.isFeatureEnabled) {
            JupiterSwapFragment.create(token, source)
        } else {
            token?.let { OrcaSwapFragment.create(it, source) } ?: OrcaSwapFragment.create(source)
        }
    }
}
