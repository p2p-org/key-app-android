package com.p2p.wowlet.fragment.notification.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentNotificationBinding
import com.p2p.wowlet.fragment.notification.dialog.EnableNotificationDialog
import com.p2p.wowlet.fragment.notification.viewmodel.NotificationViewModel
import com.p2p.wowlet.fragment.regfinish.view.RegFinishFragment
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
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