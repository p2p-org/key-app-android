package com.p2p.wowlet.fragment.dashboard.dialog.enterwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogEnterWalletBinding
import com.p2p.wowlet.fragment.dashboard.adapter.EnterWalletPagerAdapter
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.utils.viewbinding.viewBinding
import com.wowlet.entities.local.EnterWallet
import org.koin.androidx.viewmodel.ext.android.viewModel

class EnterWalletBottomSheet(val list: List<EnterWallet>) : BottomSheetDialogFragment() {

    private lateinit var adapter: EnterWalletPagerAdapter
    private val dashboardViewModel: DashboardViewModel by viewModel()

    companion object {
        const val ENTER_WALLET = "EnterWallet"
        fun newInstance(list: List<EnterWallet>): EnterWalletBottomSheet {
            return EnterWalletBottomSheet(list)
        }
    }

    private val binding: DialogEnterWalletBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_enter_wallet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EnterWalletPagerAdapter(list)
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(adapter.getInfiniteScrollingOnPageChangeCallback(binding))
        binding.indicator.attachToPager(binding.viewPager)
//        binding.pageIndicator.setViewPager(binding.viewPager)
//        adapter.registerAdapterDataObserver(binding.pageIndicator.adapterDataObserver)
    }
}