package org.p2p.wallet.debug.pushnotifications

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.settings.model.SettingsRow

interface PushNotificationsContract {

    interface View : MvpView {
        fun showNotifications(item: List<SettingsRow>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadNotifications()
        fun onNotificationClicked(@StringRes titleResId: Int)
    }
}
