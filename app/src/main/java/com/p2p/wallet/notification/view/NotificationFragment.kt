package com.p2p.wallet.notification.view

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.RegFinishFragment
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentNotificationBinding
import com.p2p.wallet.notification.dialog.EnableNotificationDialog
import com.p2p.wallet.notification.viewmodel.NotificationViewModel
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationFragment : BaseFragment(R.layout.fragment_notification) {

    companion object {
        fun newInstance() = NotificationFragment()
    }

    private val viewModel: NotificationViewModel by viewModel()
    private val binding: FragmentNotificationBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            btUseFaceID.setOnClickListener { viewModel.openEnableNotificationDialog() }
            btLater.setOnClickListener {
                viewModel.doThisLater()
                replaceFragment(RegFinishFragment())
            }
        }

        observeData()
    }

    private fun observeData() {
        viewModel.showNotificationDialog.observe(viewLifecycleOwner) {
            EnableNotificationDialog() {
                viewModel.enableNotification()
                replaceFragment(RegFinishFragment())
            }.show(
                childFragmentManager,
                EnableNotificationDialog.TAG_ENABLE_NOTIFICATION_DIALOG
            )
        }
        viewModel.isSkipNotification.observe(viewLifecycleOwner) {
            viewModel.doThisLater()
            replaceFragment(RegFinishFragment())
        }
        viewModel.isAlreadyEnableNotification.observe(viewLifecycleOwner) {
            replaceFragment(RegFinishFragment())
        }
    }
}