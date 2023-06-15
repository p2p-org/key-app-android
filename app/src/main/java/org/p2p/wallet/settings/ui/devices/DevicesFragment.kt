package org.p2p.wallet.settings.ui.devices

import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.delegates.textViewCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.sectionHeaderCellDelegate
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.animationscreen.AnimationProgressFragment
import org.p2p.wallet.auth.ui.animationscreen.TimerState
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDevicesBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding

class DevicesFragment :
    BaseMvpFragment<DevicesContract.View, DevicesContract.Presenter>(R.layout.fragment_devices),
    DevicesContract.View {

    companion object {
        fun create(): DevicesFragment = DevicesFragment()
    }

    override val presenter: DevicesContract.Presenter by inject()

    private val binding: FragmentDevicesBinding by viewBinding()

    private val adapter = CommonAnyCellAdapter(
        textViewCellDelegate(),
        sectionHeaderCellDelegate(),
        mainCellDelegate(
            inflateListener = {
                it.setOnRightFirstTextClickListener { showConfirmationDialog() }
            },
            onItemClicked = {
                if (it.rightSideCellModel != null) {
                    showConfirmationDialog()
                }
            }
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            recyclerViewDevices.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewDevices.adapter = adapter
        }
    }

    override fun showCells(cells: List<AnyCellItem>) {
        adapter.items = cells
    }

    override fun setLoadingState(isScreenLoading: Boolean) {
        if (isScreenLoading) {
            AnimationProgressFragment.show(
                fragmentManager = requireActivity().supportFragmentManager,
                timerStateList = listOf(
                    TimerState(R.string.devices_change_update_message),
                )
            )
        } else {
            AnimationProgressFragment.dismiss(requireActivity().supportFragmentManager)
        }
    }

    override fun showSuccessDeviceChange() {
        showUiKitSnackBar(message = getString(R.string.devices_change_success_message))
        popBackStack()
    }

    override fun showFailDeviceChange() {
        showUiKitSnackBar(message = getString(R.string.error_general_message))
        popBackStack()
    }

    private fun showConfirmationDialog() {
        showInfoDialog(
            titleRes = R.string.devices_confirmation_title,
            messageRes = R.string.devices_confirmation_subtitle,
            primaryButtonRes = R.string.devices_confirmation_button_title,
            primaryButtonTextColor = R.color.text_rose,
            secondaryButtonRes = R.string.common_cancel,
            primaryCallback = { presenter.executeDeviceShareChange() }
        )
    }
}