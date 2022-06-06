package org.p2p.wallet.debug.featuretoggles

import androidx.annotation.IdRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.settings.model.SettingsRow

interface FeatureTogglesContract {

    interface View : MvpView {
        fun showFeatureToggles(item: List<SettingsRow>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadFeatureToggles()
        fun onToggleCheckedListener(@IdRes toggleId: Int, toggleChecked: Boolean)
    }
}
