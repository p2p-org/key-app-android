package org.p2p.wallet.debug.featuretoggles

import org.p2p.wallet.common.feature_toggles.toggles.inapp.DebugTogglesFeatureFlag
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface FeatureTogglesContract {

    interface View : MvpView {
        fun showFeatureToggles(debugToggle: DebugTogglesFeatureFlag, toggleRows: List<FeatureToggleRowItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun onToggleChanged(toggle: FeatureToggleRowItem, newValue: String)
        fun switchDebugRemoteConfig(isDebugEnabled: Boolean)
    }
}
