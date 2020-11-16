package com.p2p.wowlet.fragment.dashboard.dialog.enterwallet

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogEnterWalletBinding
import com.p2p.wowlet.fragment.dashboard.adapter.EnterWalletPagerAdapter
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class EnterWalletBottomSheet : BottomSheetDialogFragment() {

    private val dashboardViewModel: DashboardViewModel by viewModel()
    lateinit var binding: DialogEnterWalletBinding

    companion object {
        const val ENTER_WALLET = "EnterWallet"
        fun newInstance(): EnterWalletBottomSheet {
            return EnterWalletBottomSheet()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_enter_wallet, container, false
        )
        binding.viewModel = dashboardViewModel
        binding.viewPager.adapter =  EnterWalletPagerAdapter(mutableListOf())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dismissDialog.setOnClickListener {
            dismiss()
        }
        dashboardViewModel.initReceiver()
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable=false
        }
    }
}