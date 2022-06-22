package org.p2p.wallet.debug.feature_toggles

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface FeatureTogglesContract {

    interface View : MvpView {
        fun showFeatureToggles(toggleRows: List<FeatureToggleRow>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadFeatureToggles()
        fun onToggleChanged(toggle: FeatureToggleRow, newValue: String)
    }
}
