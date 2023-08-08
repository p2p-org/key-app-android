package org.p2p.wallet.bridge.send

import androidx.fragment.app.Fragment
import org.p2p.core.token.Token
import org.p2p.wallet.bridge.send.ui.BridgeSendFragment
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.newsend.model.NetworkType
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.ui.main.SendFragment

class SendFragmentFactory(private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle) {

    fun sendFragment(searchResult: SearchResult, initialToken: Token.Active?): Fragment {
        return if (ethAddressEnabledFeatureToggle.isFeatureEnabled &&
            searchResult is SearchResult.AddressFound &&
            searchResult.networkType == NetworkType.ETHEREUM
        ) {
            BridgeSendFragment.create(recipient = searchResult, initialToken = initialToken)
        } else {
            SendFragment.create(recipient = searchResult, initialToken = initialToken)
        }
    }
}
