package org.p2p.wallet.debug.featuretoggles

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface FeatureTogglesContract {

    interface View : MvpView {
        fun showFeatureToggles(toggleRows: List<FeatureToggleRow>)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun onToggleChanged(toggle: FeatureToggleRow, newValue: String)
    }
}
