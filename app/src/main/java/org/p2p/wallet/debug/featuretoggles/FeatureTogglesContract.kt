package org.p2p.wallet.debug.featuretoggles

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.settings.model.SettingsRow

interface FeatureTogglesContract {

    interface View : MvpView {
        fun showSettings(item: List<SettingsRow>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
    }
}
