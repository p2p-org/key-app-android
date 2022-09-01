package org.p2p.wallet.debug.pushnotifications

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentPushNotificationsBinding
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.settings.ui.settings.SettingsAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class PushNotificationsFragment :
    BaseMvpFragment<PushNotificationsContract.View, PushNotificationsContract.Presenter>(
        R.layout.fragment_push_notifications
    ),
    PushNotificationsContract.View {

    companion object {
        fun create(): PushNotificationsFragment = PushNotificationsFragment()
    }

    override val presenter: PushNotificationsContract.Presenter by inject()

    private val binding: FragmentPushNotificationsBinding by viewBinding()
    private val adapter = SettingsAdapter(
        onSettingsRowClickListener = ::onNotificationClickListener,
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            pushNotificationsRecyclerView.attachAdapter(adapter)
        }

        presenter.loadNotifications()
    }

    override fun showNotifications(item: List<SettingsRow>) {
        adapter.setData(item)
    }

    private fun onNotificationClickListener(@StringRes titleResId: Int) {
        presenter.onNotificationClicked(titleResId)
    }
}
